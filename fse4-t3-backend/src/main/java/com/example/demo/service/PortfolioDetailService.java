package com.example.demo.service;

import com.example.demo.dto.ChartData;
import com.example.demo.dto.Dataset;
import com.example.demo.dto.portfolio.PortfolioPageDto;
import com.example.demo.dto.portfolio.PortfolioRowDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.*;
import com.example.demo.repository.InstrumentRepository;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.PriceRepository;
import com.example.demo.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioDetailService {

    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;
    private final PriceRepository priceRepository;
    private final TradeRepository tradeRepository;

    public PortfolioDetailService(PortfolioRepository portfolioRepository,
                                  InstrumentRepository instrumentRepository,
                                  PriceRepository priceRepository,
                                  TradeRepository tradeRepository) {
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;
        this.priceRepository = priceRepository;
        this.tradeRepository = tradeRepository;
    }

    public PortfolioPageDto getPortfolioDetails(String clientId) {
        PortfolioPageDto portfolioPageDto = new PortfolioPageDto();

        List<PortfolioRowDto> rowData = createPortfolioRowData(clientId);
        portfolioPageDto.setRowData(rowData);
        portfolioPageDto.setCategoryAllocationChart(createCategoryAllocationChart(rowData));
        portfolioPageDto.setInstrumentAllocationChart(createInstrumentAllocationChart(rowData));

        return portfolioPageDto;
    }

    public List<PortfolioRowDto> createPortfolioRowData(String clientId) {
    Portfolio portfolio = portfolioRepository.findByClientId(clientId)
        .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for client: " + clientId));
    // Ensure holdings are loaded
    Set<PortfolioHolding> holdings = portfolioRepository.findHoldingsByPortfolioId(portfolio.getClientId());
    portfolio.setHoldings(holdings != null ? holdings : new HashSet<PortfolioHolding>());

        if (portfolio.getHoldings() == null || portfolio.getHoldings().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> costBasisMap = getClientCostBasis(clientId);
        Map<String, Double> latestPricesMap = getLatestPrices();

        return portfolio.getHoldings().stream().map(holding -> {
            PortfolioRowDto row = new PortfolioRowDto();
            Instrument instrument = getInstrument(holding.getInstrumentId());

            Double currentPrice = latestPricesMap.get(holding.getInstrumentId());
            if (currentPrice == null) {
                throw new ResourceNotFoundException("Latest price not found for instrument: " + holding.getInstrumentId());
            }

            double costBasisPerShare = costBasisMap.getOrDefault(holding.getInstrumentId(), 0.0);
            double totalCost = costBasisPerShare * holding.getQuantity();
            double marketValue = currentPrice * holding.getQuantity();
            double unrealizedPl = marketValue - totalCost;
            double unrealizedPlPercent = (totalCost > 0) ? (unrealizedPl / totalCost) * 100 : 0;

            row.setInstrumentId(holding.getInstrumentId());
            row.setInstrumentName(instrument.getDescription());
            row.setQuantity(holding.getQuantity());
            row.setCategoryId(instrument.getCategoryId());
            row.setMarketValue(marketValue);
            row.setCostBasis(costBasisPerShare);
            row.setTotalCost(totalCost);
            row.setUnrealizedPl(unrealizedPl);
            row.setUnrealizedPlPercent(unrealizedPlPercent);

            return row;
        }).collect(Collectors.toList());
    }

    private ChartData createCategoryAllocationChart(List<PortfolioRowDto> rowData) {
        Map<String, Double> categoryValues = rowData.stream()
                .collect(Collectors.groupingBy(PortfolioRowDto::getCategoryId,
                        Collectors.summingDouble(PortfolioRowDto::getMarketValue)));

        ChartData chart = new ChartData();
        chart.setLabels(new ArrayList<>(categoryValues.keySet()));
        Dataset dataset = new Dataset();
        dataset.setData(new ArrayList<>(categoryValues.values()));
        dataset.setBackgroundColor(Arrays.asList("#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"));
        chart.setDatasets(Collections.singletonList(dataset));
        return chart;
    }

    private ChartData createInstrumentAllocationChart(List<PortfolioRowDto> rowData) {
        Map<String, Double> instrumentValues = rowData.stream()
                .collect(Collectors.groupingBy(PortfolioRowDto::getInstrumentName,
                        Collectors.summingDouble(PortfolioRowDto::getMarketValue)));

        ChartData chart = new ChartData();
        chart.setLabels(new ArrayList<>(instrumentValues.keySet()));
        Dataset dataset = new Dataset();
        dataset.setData(new ArrayList<>(instrumentValues.values()));
        dataset.setBackgroundColor(Arrays.asList("#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40", "#E7E9ED", "#8A2BE2", "#A52A2A", "#DEB887", "#5F9EA0", "#7FFF00"));
        chart.setDatasets(Collections.singletonList(dataset));
        return chart;
    }

    private Map<String, Double> getClientCostBasis(String clientId) {
        List<Trade> trades = tradeRepository.findByClientId(clientId);

        return trades.stream()
                .filter(t -> t.getDirection().equals("BUY"))
                .collect(Collectors.groupingBy(
                        Trade::getInstrumentId,
                        Collectors.averagingDouble(Trade::getExecutionPrice)
                ));
    }

    private Map<String, Double> getLatestPrices() {
        return priceRepository.findLatestPrices().stream()
                .collect(Collectors.toMap(
                        Price::getInstrumentId,
                        Price::getAskPrice,
                        (price1, price2) -> price2
                ));
    }

    private Instrument getInstrument(String instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found: " + instrumentId));
    }
}
