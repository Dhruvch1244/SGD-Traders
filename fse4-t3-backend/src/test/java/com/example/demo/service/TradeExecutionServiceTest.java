package com.example.demo.service;

import com.example.demo.dto.trade.TradeRequestDto;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExecutionServiceTest {

    @Mock WalletRepository walletRepository;
    @Mock PortfolioRepository portfolioRepository;
    @Mock InstrumentRepository instrumentRepository;
    @Mock PriceRepository priceRepository;
    @Mock TradeRepository tradeRepository;
    @Mock OrderRepository orderRepository;
    @Mock RestTemplate restTemplate;

    @InjectMocks TradeExecutionService service;

    private TradeRequestDto req;
    private Wallet wallet;
    private Instrument instrument;
    private Price price;
    private Portfolio portfolio;

    @BeforeEach
    void setup() {
        req = new TradeRequestDto();
        req.setClientId("C1"); // legacy, not used for DB
        req.setLocalClientId("C1"); // used for DB
        req.setFmtsClientId("FMTS1"); // used for FMTS
        req.setInstrumentId("INST1");
        req.setQuantity(10);
        req.setTargetPrice(100.0);
        req.setDirection("B");
        req.setEmail("test@example.com");
        req.setToken("token123");

        wallet = new Wallet("C1", 10000.0);
        instrument = new Instrument("INST1", "Desc", "T", "EID", "CAT", 1, 1000);
        price = new Price("INST1", 100.0, 99.0, LocalDateTime.now());
        portfolio = new Portfolio("C1", new HashSet<>());
    }

    @Test
    void buy_insufficientFunds_throws() {
        wallet.setBalance(100.0);
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPriceForInstrument("INST1")).thenReturn(Optional.of(price));
        assertThrows(IllegalArgumentException.class, () -> service.executeBuyTrade(req));
    }

    @Test
    void sell_insufficientHoldings_throws() {
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));
        // holdings returned by repository call in getPortfolio
        when(portfolioRepository.findHoldingsByPortfolioId("C1")).thenReturn(new java.util.HashSet<>(portfolio.getHoldings()));
        assertThrows(IllegalArgumentException.class, () -> service.executeSellTrade(req));
    }

    @Test
    void buy_success_savesTradeAndWalletAndPortfolio() {
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPriceForInstrument("INST1")).thenReturn(Optional.of(price));
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));

        // Mock FMTS verification
        var fmtsMap = new java.util.HashMap<String, Object>();
        fmtsMap.put("tradeId", "FMTS_TRADE_ID");
        fmtsMap.put("executionPrice", 100.0);
        fmtsMap.put("cashValue", 1000.0);
        TradeExecutionService spyService = spy(service);
        doReturn(fmtsMap).when(spyService).verifyTradeWithFmts(any(), any());

        doNothing().when(orderRepository).insert(any());
        doNothing().when(tradeRepository).insert(any());

        var trade = spyService.executeBuyTrade(req);
        assertNotNull(trade);
        assertEquals("FMTS_TRADE_ID", trade.getTradeId());
        assertNotNull(trade.getOrderId());
        verify(walletRepository).update(any());
        verify(tradeRepository).insert(any());
        verify(orderRepository).insert(any());
    }

    @Test
    void sell_success_updatesWalletAndPortfolioAndInsertsTradeAndOrder() {
        // Setup portfolio with holdings
        HashSet<PortfolioHolding> holdings = new HashSet<>();
        PortfolioHolding holding = new PortfolioHolding();
        holding.setInstrumentId("INST1");
        holding.setQuantity(15);
        holdings.add(holding);
        portfolio.setHoldings(holdings);

        // Only required mocks for sell path
        when(portfolioRepository.findHoldingsByPortfolioId("C1")).thenReturn(holdings);
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));

        // Mock FMTS verification
        var fmtsMap = new java.util.HashMap<String, Object>();
        fmtsMap.put("tradeId", "FMTS_SELL_ID");
        fmtsMap.put("executionPrice", 99.0);
        fmtsMap.put("cashValue", 990.0);
        TradeExecutionService spyService = spy(service);
        doReturn(fmtsMap).when(spyService).verifyTradeWithFmts(any(), any());

        doNothing().when(orderRepository).insert(any());
        doNothing().when(tradeRepository).insert(any());
        doNothing().when(walletRepository).update(any());
        doNothing().when(portfolioRepository).deleteHoldings(any());
        doNothing().when(portfolioRepository).insertHolding(any());

        var trade = spyService.executeSellTrade(req);
        assertNotNull(trade);
        assertEquals("FMTS_SELL_ID", trade.getTradeId());
        assertNotNull(trade.getOrderId());
        verify(walletRepository).update(any());
        verify(tradeRepository).insert(any());
        verify(orderRepository).insert(any());
        verify(portfolioRepository).deleteHoldings(any());
        verify(portfolioRepository, atLeastOnce()).insertHolding(any());
    }

    @Test
    void buy_fmtsVerificationFails_throws() {
        // Only stub what's needed for the exception
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPriceForInstrument("INST1")).thenReturn(Optional.of(price));
        TradeExecutionService spyService = spy(service);
        doReturn(null).when(spyService).verifyTradeWithFmts(any(), any());
        assertThrows(IllegalArgumentException.class, () -> spyService.executeBuyTrade(req));
    }

    @Test
    void buy_missingInstrument_throws() {
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.executeBuyTrade(req));
    }

    @Test
    void buy_missingPrice_throws() {
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPriceForInstrument("INST1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.executeBuyTrade(req));
    }

    @Test
    void buy_zeroQuantity_throws() {
        req.setQuantity(0);
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        when(instrumentRepository.findById("INST1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPriceForInstrument("INST1")).thenReturn(Optional.of(price));
        assertThrows(IllegalArgumentException.class, () -> service.executeBuyTrade(req));
    }

    @Test
    void sell_missingPortfolio_throws() {
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.executeSellTrade(req));
    }

    @Test
    void sell_missingWallet_throws() {
        HashSet<PortfolioHolding> holdings = new HashSet<>();
        PortfolioHolding holding = new PortfolioHolding();
        holding.setInstrumentId("INST1");
        holding.setQuantity(15);
        holdings.add(holding);
        portfolio.setHoldings(holdings);
        when(portfolioRepository.findHoldingsByPortfolioId("C1")).thenReturn(holdings);
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.executeSellTrade(req));
    }

    @Test
    void sell_zeroQuantity_throws() {
        req.setQuantity(0);
        // Only stub what's needed for the exception
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));
        assertThrows(IllegalArgumentException.class, () -> service.executeSellTrade(req));
    }

    @Test
    void sell_partialSell_updatesHolding() {
        req.setQuantity(5);
        HashSet<PortfolioHolding> holdings = new HashSet<>();
        PortfolioHolding holding = new PortfolioHolding();
        holding.setInstrumentId("INST1");
        holding.setQuantity(10);
        holdings.add(holding);
        portfolio.setHoldings(holdings);
        when(portfolioRepository.findHoldingsByPortfolioId("C1")).thenReturn(holdings);
        when(portfolioRepository.findByClientId("C1")).thenReturn(Optional.of(portfolio));
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(wallet));
        var fmtsMap = new java.util.HashMap<String, Object>();
        fmtsMap.put("tradeId", "FMTS_SELL_ID");
        fmtsMap.put("executionPrice", 99.0);
        fmtsMap.put("cashValue", 495.0);
        TradeExecutionService spyService = spy(service);
        doReturn(fmtsMap).when(spyService).verifyTradeWithFmts(any(), any());
        doNothing().when(orderRepository).insert(any());
        doNothing().when(tradeRepository).insert(any());
        doNothing().when(walletRepository).update(any());
        doNothing().when(portfolioRepository).deleteHoldings(any());
        doNothing().when(portfolioRepository).insertHolding(any());
        var trade = spyService.executeSellTrade(req);
        assertNotNull(trade);
        assertEquals("FMTS_SELL_ID", trade.getTradeId());
        assertEquals(5, holding.getQuantity()); // After partial sell, holding should be updated
        verify(portfolioRepository, atLeastOnce()).insertHolding(any());
        verify(portfolioRepository, atLeastOnce()).deleteHoldings(any()); // Accept deletion for update
    }

    // --- verifyTradeWithFmts Tests ---
    @Test
    void verifyTradeWithFmts_happyPath() throws Exception {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        String fmtsJson = "{\"tradeId\":\"FMTS_TRADE_ID\",\"executionPrice\":100.0,\"cashValue\":1000.0}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        Map<String, Object> result = service.verifyTradeWithFmts(dto, "B");
        assertEquals("FMTS_TRADE_ID", result.get("tradeId"));
        assertEquals(100.0, Double.parseDouble(result.get("executionPrice").toString()));
        assertEquals(1000.0, Double.parseDouble(result.get("cashValue").toString()));
    }

    @Test
    void verifyTradeWithFmts_missingToken_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken(null);
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_missingFmtsClientId_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId(null);
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_missingEmail_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail(null);
        dto.setToken("token123");
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_non2xxResponse_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        ResponseEntity<String> response = ResponseEntity.status(400).body(null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_nullBody_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        ResponseEntity<String> response = ResponseEntity.ok(null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_missingFields_throws() throws Exception {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        String fmtsJson = "{\"cashValue\":1000.0}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }

    @Test
    void verifyTradeWithFmts_exception_throws() {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setInstrumentId("INST1");
        dto.setQuantity(10);
        dto.setTargetPrice(100.0);
        dto.setDirection("B");
        dto.setFmtsClientId("FMTS1");
        dto.setEmail("test@example.com");
        dto.setToken("token123");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("FMTS error"));
        TradeExecutionService service = new TradeExecutionService(walletRepository, portfolioRepository, instrumentRepository, priceRepository, tradeRepository, orderRepository, restTemplate);
        assertThrows(IllegalArgumentException.class, () -> service.verifyTradeWithFmts(dto, "B"));
    }
}
