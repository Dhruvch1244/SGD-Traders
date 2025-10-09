package com.example.demo.service;

import com.example.demo.dto.trade.TradeRequestDto;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class TradeExecutionService {

    private final WalletRepository walletRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;
    private final PriceRepository priceRepository;
    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${fmts.trade.url}")
    private String fmtsTradeUrl;

    public TradeExecutionService(WalletRepository walletRepository, PortfolioRepository portfolioRepository,
                                 InstrumentRepository instrumentRepository, PriceRepository priceRepository,
                                 TradeRepository tradeRepository, OrderRepository orderRepository,
                                 RestTemplate restTemplate) {
        this.walletRepository = walletRepository;
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;
        this.priceRepository = priceRepository;
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    protected Map<String, Object> verifyTradeWithFmts(TradeRequestDto request, String direction) {
        // Use FMTS fields directly from request
        if (request.getToken() == null || request.getFmtsClientId() == null || request.getEmail() == null) {
            throw new IllegalArgumentException("FMTS credentials missing in request");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of(
            "instrumentId", request.getInstrumentId(),
            "quantity", request.getQuantity(),
            "targetPrice", request.getTargetPrice(),
            "direction", direction,
            "clientId", request.getFmtsClientId(),
            "email", request.getEmail(),
            "token", request.getToken()
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fmtsTradeUrl, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("FMTS trade verification failed: No response");
            }
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> fmtsTrade = mapper.readValue(response.getBody(), Map.class);
            if (fmtsTrade.get("tradeId") == null || fmtsTrade.get("executionPrice") == null) {
                throw new IllegalArgumentException("FMTS trade verification failed: Missing fields");
            }
            return fmtsTrade;
        } catch (Exception e) {
            throw new IllegalArgumentException("FMTS trade verification failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Trade executeBuyTrade(TradeRequestDto request) {
        Wallet wallet = getWallet(request.getLocalClientId());
        Instrument instrument = getInstrument(request.getInstrumentId());
        Price latestPrice = getLatestPrice(request.getInstrumentId());

        double cost = latestPrice.getAskPrice() * request.getQuantity();
        if(request.getQuantity() < instrument.getMinQuantity() || request.getQuantity() > instrument.getMaxQuantity()){
            throw new IllegalArgumentException("Quantity out of range");
        }
        if (wallet.getBalance() < cost) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // FMTS verification
        Map<String, Object> fmtsTrade = verifyTradeWithFmts(request, "B");
        if (fmtsTrade == null) {
            throw new IllegalArgumentException("FMTS verification failed");
        }
        double executionPrice = Double.parseDouble(fmtsTrade.get("executionPrice").toString());
        String fmtsTradeId = fmtsTrade.get("tradeId").toString();
        double cashValue = Double.parseDouble(fmtsTrade.get("cashValue").toString());
        wallet.setBalance(wallet.getBalance() - cashValue);
        walletRepository.update(wallet);

        updatePortfolio(request.getLocalClientId(), request.getInstrumentId(), request.getQuantity(), true);
         Order order = createOrderRecord(request, executionPrice, "BUY");
        if (order.getOrderId() == null) {
            throw new IllegalStateException("OrderId is null before insert. This should never happen.");
        }
        orderRepository.insert(order);

        Trade trade = createTradeRecord(request, executionPrice, "BUY", cashValue);
        trade.setOrderId(order.getOrderId()); // Set orderId in trade
        trade.setOrder(order); // Set the full order object for response
        trade.setTradeId(fmtsTradeId); // Use FMTS tradeId
        tradeRepository.insert(trade);
        return trade;
    }

    @Transactional
    public Trade executeSellTrade(TradeRequestDto request) {
        Portfolio portfolio = getPortfolio(request.getLocalClientId());
        int currentHoldings = getHoldingQuantity(portfolio, request.getInstrumentId());

        if(request.getQuantity() < 1){
            throw new IllegalArgumentException("Quantity out of range");
        }

        if (currentHoldings < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient holdings");
        }
        Wallet wallet = getWallet(request.getLocalClientId());

        // FMTS verification
//        Map<String, Object> fmtsTrade = verifyTradeWithFmts(request, "S");
//        if (fmtsTrade == null) {
//            throw new IllegalArgumentException("FMTS verification failed");
//        }
        double executionPrice = request.getTargetPrice();
        String fmtsTradeId = java.util.UUID.randomUUID().toString();
        double cashValue = request.getTargetPrice()* request.getQuantity();
        wallet.setBalance(wallet.getBalance() + cashValue);
        walletRepository.update(wallet);
        Order order = createOrderRecord(request, executionPrice, "SELL");
        orderRepository.insert(order);

        updatePortfolio(request.getLocalClientId(), request.getInstrumentId(), request.getQuantity(), false);

        Trade trade = createTradeRecord(request, executionPrice, "SELL", cashValue);
        trade.setOrderId(order.getOrderId()); // Set orderId in trade (like buy)
        trade.setOrder(order); // Set the full order object for response
        trade.setTradeId(fmtsTradeId); // Use FMTS tradeId
        tradeRepository.insert(trade);
        return trade;
    }

    private Order createOrderRecord(TradeRequestDto request, double price, String direction) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setInstrumentId(request.getInstrumentId());
        order.setQuantity(request.getQuantity());
        order.setTargetPrice(price);
        order.setDirection(direction);
        order.setClientId(request.getLocalClientId()); // Use localClientId for DB
        return order;
    }

    private void updatePortfolio(String clientId, String instrumentId, int quantity, boolean isBuy) {
        int delta = isBuy ? quantity : -quantity;
        // Check if holding exists
        Optional<PortfolioHolding> holdingOpt = portfolioRepository.findHolding(clientId, instrumentId);
        if (holdingOpt.isPresent()) {
            // update existing holding
            PortfolioHolding holding = holdingOpt.get();
            holding.setQuantity(holding.getQuantity() + delta);
            portfolioRepository.updateHolding(holding);
        } else if (isBuy && quantity > 0) {
            // insert new holding
            PortfolioHolding newHolding = new PortfolioHolding();
            newHolding.setPortfolioId(clientId);
            newHolding.setInstrumentId(instrumentId);
            newHolding.setQuantity(quantity);
            portfolioRepository.insertHolding(newHolding);
        }

    }

    private Trade createTradeRecord(TradeRequestDto request, double price, String direction, double cashValue) {
        Trade trade = new Trade();
        trade.setTradeId(UUID.randomUUID().toString());
        trade.setClientId(request.getLocalClientId()); // Use localClientId for DB
        trade.setInstrumentId(request.getInstrumentId());
        trade.setQuantity(request.getQuantity());
        trade.setExecutionPrice(price);
        trade.setDirection(direction);
        trade.setCashValue(cashValue);
        trade.setTimestamp(LocalDateTime.now());
        return trade;
    }

    private Wallet getWallet(String clientId) {
        return walletRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for client: " + clientId));
    }

    private Portfolio getPortfolio(String clientId) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findByClientId(clientId);
        if (portfolioOpt.isPresent()) {
            Portfolio portfolio = portfolioOpt.get();
            Set<PortfolioHolding> holdings = portfolioRepository.findHoldingsByPortfolioId(clientId);
            if (holdings == null) {
                portfolio.setHoldings(new HashSet<>());
            } else {
                portfolio.setHoldings(holdings);
            }
            return portfolio;
        } else {
            Portfolio newPortfolio = new Portfolio(clientId);
            portfolioRepository.insert(newPortfolio);
            return newPortfolio;
        }
    }

    private int getHoldingQuantity(Portfolio portfolio, String instrumentId) {
        return portfolio.getHoldings().stream()
                .filter(h -> h.getInstrumentId().equals(instrumentId))
                .mapToInt(PortfolioHolding::getQuantity)
                .sum();
    }

    private Instrument getInstrument(String instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + instrumentId));
    }

    private Price getLatestPrice(String instrumentId) {
        return priceRepository.findLatestPriceForInstrument(instrumentId)
                .orElseThrow(() -> new IllegalArgumentException("Price not found for instrument: " + instrumentId));
    }

    // Custom exceptions for controller mapping
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }
    public static class InvalidEmailException extends RuntimeException {
        public InvalidEmailException(String message) { super(message); }
    }
    public static class PriceToleranceException extends RuntimeException {
        public PriceToleranceException(String message) { super(message); }
    }
}
