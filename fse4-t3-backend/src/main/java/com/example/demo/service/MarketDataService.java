package com.example.demo.service;

import com.example.demo.dto.market.MarketPerformerDto;
import com.example.demo.dto.market.MostActiveStockDto;
import com.example.demo.dto.market.TradePageInstrumentDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.Instrument;
import com.example.demo.models.PortfolioHolding;
import com.example.demo.models.Price;
import com.example.demo.models.Trade;
import com.example.demo.repository.InstrumentRepository;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.PriceRepository;
import com.example.demo.repository.TradeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MarketDataService {

    private final InstrumentRepository instrumentRepository;
    private final PriceRepository priceRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;

    public MarketDataService(InstrumentRepository instrumentRepository,
                             PriceRepository priceRepository,
                             TradeRepository tradeRepository,
                             PortfolioRepository portfolioRepository) {
        this.instrumentRepository = instrumentRepository;
        this.priceRepository = priceRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public List<TradePageInstrumentDto> getInstrumentsForTradePage(String clientId) {
        Map<String, Price> latestPrices = getLatestPricesMap();

        Set<String> clientInstrumentIds = new HashSet<>();
        if (clientId != null && !clientId.isBlank()) {
            portfolioRepository.findByClientId(clientId)
                    .ifPresent(portfolio -> {
                        if (portfolio.getHoldings() != null) {
                            portfolio.getHoldings().stream()
                                    .map(PortfolioHolding::getInstrumentId)
                                    .forEach(clientInstrumentIds::add);
                        }
                    });
        }

        List<Instrument> instruments = instrumentRepository.findAll();

        return instruments.stream().map(instrument -> {
            TradePageInstrumentDto dto = new TradePageInstrumentDto();
            Price price = latestPrices.get(instrument.getInstrumentId());

            dto.setInstrumentId(instrument.getInstrumentId());
            dto.setDescription(instrument.getDescription());
            dto.setMinQuantity(instrument.getMinQuantity());
            dto.setMaxQuantity(instrument.getMaxQuantity());
            if (price != null) {
                dto.setAskPrice(price.getAskPrice());
                dto.setBidPrice(price.getBidPrice());
            }
            dto.setInPortfolio(clientInstrumentIds.contains(instrument.getInstrumentId()));

            return dto;
        }).collect(Collectors.toList());
    }

    public List<MarketPerformerDto> getTopGainers() {
        return calculatePerformers().stream()
                .filter(p -> p.getPriceChange() > 0)
                .sorted(Comparator.comparing(MarketPerformerDto::getPercentChange).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<MarketPerformerDto> getTopLosers() {
        return calculatePerformers().stream()
                .filter(p -> p.getPriceChange() < 0)
                .sorted(Comparator.comparing(MarketPerformerDto::getPercentChange))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<MostActiveStockDto> getMostActiveStocks() {
        List<Trade> allTrades = tradeRepository.findAll();

        Map<String, Long> volumeByInstrument = allTrades.stream()
                .collect(Collectors.groupingBy(Trade::getInstrumentId, Collectors.summingLong(Trade::getQuantity)));

        return volumeByInstrument.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    MostActiveStockDto dto = new MostActiveStockDto();
                    Instrument instrument = getInstrument(entry.getKey());
                    dto.setInstrumentId(entry.getKey());
                    dto.setDescription(instrument.getDescription());
                    dto.setTotalVolume(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<MarketPerformerDto> calculatePerformers() {
        List<Price> allPrices = priceRepository.findAll();

        Map<String, List<Price>> pricesByInstrument = allPrices.stream()
                .collect(Collectors.groupingBy(Price::getInstrumentId));

        return pricesByInstrument.entrySet().stream().map(entry -> {
            List<Price> prices = entry.getValue();
            if (prices.size() < 2) return null;

            prices.sort(Comparator.comparing(Price::getTimestamp).reversed());
            Price latestPrice = prices.get(0);
            Price previousPrice = prices.get(1);

            double priceChange = latestPrice.getAskPrice() - previousPrice.getAskPrice();
            double percentChange = (previousPrice.getAskPrice() > 0) ? (priceChange / previousPrice.getAskPrice()) * 100 : 0;

            MarketPerformerDto performer = new MarketPerformerDto();
            Instrument instrument = getInstrument(entry.getKey());
            performer.setInstrumentId(entry.getKey());
            performer.setDescription(instrument.getDescription());
            performer.setCurrentPrice(latestPrice.getAskPrice());
            performer.setPriceChange(priceChange);
            performer.setPercentChange(percentChange);
            return performer;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    Map<String, Price> getLatestPricesMap() {
        return priceRepository.findLatestPrices().stream()
                .collect(Collectors.toMap(Price::getInstrumentId, p -> p));
    }

    private Instrument getInstrument(String instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found: " + instrumentId));
    }
}
