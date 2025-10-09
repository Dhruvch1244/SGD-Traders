package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.*;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;
    private final InstrumentService instrumentService;
    private final PriceService priceService;

    public PortfolioService(PortfolioRepository portfolioRepository, TradeRepository tradeRepository,
                            InstrumentService instrumentService, PriceService priceService) {
        this.portfolioRepository = portfolioRepository;
        this.tradeRepository = tradeRepository;
        this.instrumentService = instrumentService;
        this.priceService = priceService;
    }

    public Portfolio getPortfolioByClientId(String clientId) {
        return portfolioRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio(clientId);
                    portfolioRepository.insert(newPortfolio);
                    return newPortfolio;
                });
    }

    public PortfolioSummary getPortfolioSummary(String clientId) {
        Portfolio portfolio = getPortfolioByClientId(clientId);

        Set<PortfolioHolding> holdings = portfolioRepository.findHoldingsByPortfolioId(portfolio.getClientId());
        portfolio.setHoldings(holdings != null ? holdings : new HashSet<PortfolioHolding>());

        PortfolioSummary summary = new PortfolioSummary();

        Summary portfolioSummaryData = createPortfolioSummary(portfolio);
        summary.setPortfolioSummary(portfolioSummaryData);

        summary.setLineChartData(createLineChartData(clientId, portfolioSummaryData));
        summary.setPieChartData(createPieChartData(portfolio));
        summary.setPerformanceColumns(createPerformanceColumns());
        List<PerformanceData> performanceData = createPerformanceData(clientId, portfolioSummaryData);
        summary.setPerformanceData(performanceData);
        summary.setBarChartData(createBarChartData(performanceData));
        summary.setTradeHistoryColumns(createTradeHistoryColumns());
        summary.setTradeHistoryData(createTradeHistoryData(clientId));

        return summary;
    }

    public Summary createPortfolioSummary(Portfolio portfolio) {
        Summary summary = new Summary();

        if (portfolio.getHoldings() == null || portfolio.getHoldings().isEmpty()) {
            summary.setTotalValue(0.0);
            summary.setTotalGainLoss(0.0);
            return summary;
        }

        List<Trade> clientTrades = tradeRepository.findByClientId(portfolio.getClientId());

        Map<String, Double> avgCostPerShare = clientTrades.stream()
                .filter(t -> "BUY".equals(t.getDirection()))
                .collect(Collectors.groupingBy(
                        Trade::getInstrumentId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                trades -> {
                                    double totalCost = trades.stream()
                                            .mapToDouble(t -> t.getQuantity() * t.getExecutionPrice()).sum();
                                    int totalQuantity = trades.stream().mapToInt(Trade::getQuantity).sum();
                                    return totalQuantity > 0 ? totalCost / totalQuantity : 0.0;
                                })));

        double totalValue = 0;
        double totalCostOfCurrentHoldings = 0;

        for (PortfolioHolding holding : portfolio.getHoldings()) {
            Price latestPrice = priceService.getLatestPriceForInstrument(holding.getInstrumentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Latest price not found for instrument: " + holding.getInstrumentId()));
            totalValue += holding.getQuantity() * latestPrice.getAskPrice();

            double avgCost = avgCostPerShare.getOrDefault(holding.getInstrumentId(), 0.0);
            totalCostOfCurrentHoldings += holding.getQuantity() * avgCost;
        }

        summary.setTotalValue(totalValue);
        summary.setTotalGainLoss(totalValue - totalCostOfCurrentHoldings);
        return summary;
    }

    public ChartData createLineChartData(String clientId, Summary currentSummary) {
        List<Trade> clientTrades = tradeRepository.findByClientId(clientId);

        ChartData lineChart = new ChartData();
        if (clientTrades.isEmpty()) {
            lineChart.setLabels(Collections.emptyList());
            lineChart.setDatasets(Collections.emptyList());
            return lineChart;
        }

        double currentTotalValue = currentSummary.getTotalValue();

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        Random random = new Random(clientId.hashCode());

        double startValue = currentTotalValue * (0.8 + random.nextDouble() * 0.15);
        double step = (currentTotalValue - startValue) / 9;

        for (int i = 9; i >= 0; i--) {
            labels.add(LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            double pointValue = startValue + step * (9 - i);
            pointValue *= (1 + (random.nextDouble() - 0.5) * 0.05);
            data.add(Math.max(0, pointValue));
        }
        lineChart.setLabels(labels);
        Dataset dataset = new Dataset();
        dataset.setLabel("Portfolio Value");
        dataset.setData(data);
        dataset.setBorderColor("#42a5f5");
        dataset.setFill(false);
        lineChart.setDatasets(Collections.singletonList(dataset));
        return lineChart;
    }

    public ChartData createPieChartData(Portfolio portfolio) {
        ChartData pieChart = new ChartData();
        Map<String, Double> allocation = new HashMap<>();

        if (portfolio != null && portfolio.getHoldings() != null) {
            for (PortfolioHolding holding : portfolio.getHoldings()) {
                Instrument instrument = instrumentService.getInstrumentById(holding.getInstrumentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Instrument not found: " + holding.getInstrumentId()));
                Price latestPrice = priceService.getLatestPriceForInstrument(instrument.getInstrumentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Latest price not found for instrument: " + instrument.getInstrumentId()));

                double value = holding.getQuantity() * latestPrice.getAskPrice();
                allocation.merge(instrument.getCategoryId(), value, Double::sum);
            }
        }

        pieChart.setLabels(new ArrayList<>(allocation.keySet()));
        Dataset dataset = new Dataset();
        dataset.setData(new ArrayList<>(allocation.values()));
        dataset.setBackgroundColor(Arrays.asList("#42a5f5", "#66bb6a", "#ef5350", "#ffa726", "#ab47bc"));
        pieChart.setDatasets(Collections.singletonList(dataset));
        return pieChart;
    }

    public List<ColumnDefinition> createPerformanceColumns() {
        return Arrays.asList(
                createColumn("Date", "date"),
                createColumn("Daily Gain/Loss", "gainLoss"),
                createColumn("Volume", "volume"));
    }

    public List<PerformanceData> createPerformanceData(String clientId, Summary currentSummary) {
        List<Trade> clientTrades = tradeRepository.findByClientId(clientId);

        if (clientTrades.isEmpty()) {
            return Collections.emptyList();
        }

        List<PerformanceData> performance = new ArrayList<>();
        Random random = new Random(clientId.hashCode());
        double totalValue = currentSummary.getTotalValue();
        double dailyChangeMagnitude = totalValue * 0.01;

        for (int i = 0; i < 10; i++) {
            PerformanceData pd = new PerformanceData();
            pd.setDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            pd.setGainLoss((random.nextDouble() - 0.5) * 2 * dailyChangeMagnitude);
            pd.setVolume(random.nextInt(clientTrades.size() * 100) + 50);
            performance.add(pd);
        }
        return performance;
    }

    public ChartData createBarChartData(List<PerformanceData> performanceData) {
        ChartData barChart = new ChartData();
        List<String> labels = performanceData.stream().map(PerformanceData::getDate).collect(Collectors.toList());
        List<Double> data = performanceData.stream().map(PerformanceData::getGainLoss).collect(Collectors.toList());
        Collections.reverse(labels);
        Collections.reverse(data);
        barChart.setLabels(labels);
        Dataset dataset = new Dataset();
        dataset.setLabel("Daily Gain/Loss");
        dataset.setData(data);
        dataset.setBackgroundColor("#66bb6a");
        barChart.setDatasets(Collections.singletonList(dataset));
        return barChart;
    }

    public List<ColumnDefinition> createTradeHistoryColumns() {
        return Arrays.asList(
                createColumn("Date", "date"),
                createColumn("Asset", "asset"),
                createColumn("Type", "type"),
                createColumn("Amount", "amount"),
                createColumn("Price", "price"));
    }

    public List<TradeHistoryData> createTradeHistoryData(String clientId) {
        List<Trade> clientTrades = tradeRepository.findByClientId(clientId);
        return clientTrades.stream()
                .sorted(Comparator.comparing(Trade::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(15)
                .map(trade -> {
                    TradeHistoryData thd = new TradeHistoryData();
                    if (trade.getTimestamp() != null) {
                        thd.setDate(trade.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
                    } else {
                        thd.setDate("N/A");
                    }
                    Instrument instrument = instrumentService.getInstrumentById(trade.getInstrumentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Instrument not found for trade "
                                    + trade.getId() + ": " + trade.getInstrumentId()));
                    thd.setAsset(instrument.getDescription());
                    thd.setType(trade.getDirection());
                    thd.setAmount(trade.getQuantity());
                    thd.setPrice(trade.getExecutionPrice());
                    return thd;
                })
                .collect(Collectors.toList());
    }

    public ColumnDefinition createColumn(String headerName, String field) {
        ColumnDefinition column = new ColumnDefinition();
        column.setHeaderName(headerName);
        column.setField(field);
        return column;
    }
}
