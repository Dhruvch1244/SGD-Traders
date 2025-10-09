package com.example.demo.service;

import com.example.demo.dto.PortfolioSummary;
import com.example.demo.dto.Summary;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.*;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PortfolioServiceTest {
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private InstrumentService instrumentService;
    @Mock
    private PriceService priceService;
    @InjectMocks
    private PortfolioService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testServiceInstantiation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("getPortfolioSummary throws ResourceNotFoundException when portfolio not found")
    void testGetPortfolioSummaryThrowsWhenPortfolioNotFound() {
        when(portfolioRepository.findByClientId(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getPortfolioSummary("unknown"));
    }

    @Test
    @DisplayName("createPortfolioSummary throws ResourceNotFoundException when price not found")
    void testCreatePortfolioSummaryThrowsWhenPriceNotFound() {
        Portfolio portfolio = new Portfolio("c1");
        portfolio.setHoldings(Set.of(new PortfolioHolding(1L, "c1", "inst1", 10)));
        when(priceService.getLatestPriceForInstrument("inst1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createPortfolioSummary(portfolio));
    }

    @Test
    @DisplayName("createPortfolioSummary calculates summary correctly")
    void testCreatePortfolioSummaryCalculatesCorrectly() {
        Portfolio portfolio = new Portfolio("c1");
        portfolio.setHoldings(Set.of(new PortfolioHolding(1L, "c1", "inst1", 10)));

        Trade trade = new Trade("t1", "inst1", 10, 90.0, "BUY", "c1", "o1", 900.0, LocalDateTime.now(), true);
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.singletonList(trade));

        Price price = new Price("inst1", 100.0, 100.0, LocalDateTime.now());
        when(priceService.getLatestPriceForInstrument("inst1")).thenReturn(Optional.of(price));

        Summary summary = service.createPortfolioSummary(portfolio);

        assertEquals(1000.0, summary.getTotalValue());
        assertEquals(100.0, summary.getTotalGainLoss());
    }

    @Test
    @DisplayName("createPortfolioSummary handles no holdings")
    void testCreatePortfolioSummaryHandlesNoHoldings() {
        Portfolio portfolio = new Portfolio("c1");
        portfolio.setHoldings(new HashSet<>());

        Summary summary = service.createPortfolioSummary(portfolio);

        assertEquals(0.0, summary.getTotalValue());
        assertEquals(0.0, summary.getTotalGainLoss());
    }

    @Test
    @DisplayName("getPortfolioSummary returns full summary")
    void testGetPortfolioSummaryHappyPath() {
        Portfolio portfolio = new Portfolio("c1");
        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId(anyString())).thenReturn(new HashSet<>());

        PortfolioSummary summary = service.getPortfolioSummary("c1");

        assertNotNull(summary);
        assertNotNull(summary.getPortfolioSummary());
        assertNotNull(summary.getLineChartData());
        assertNotNull(summary.getPieChartData());
        assertNotNull(summary.getPerformanceColumns());
        assertNotNull(summary.getPerformanceData());
        assertNotNull(summary.getBarChartData());
        assertNotNull(summary.getTradeHistoryColumns());
        assertNotNull(summary.getTradeHistoryData());
    }

    @Test
    @DisplayName("createLineChartData with no trades returns empty chart")
    void testCreateLineChartDataWithNoTrades() {
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.emptyList());
        Summary summary = new Summary();
        var chart = service.createLineChartData("c1", summary);
        assertNotNull(chart);
        assertTrue(chart.getLabels().isEmpty());
        assertTrue(chart.getDatasets().isEmpty());
    }

    @Test
    @DisplayName("createLineChartData with trades returns chart data")
    void testCreateLineChartDataWithTrades() {
        Trade trade = new Trade("t1", "inst1", 10, 90.0, "BUY", "c1", "o1", 900.0, LocalDateTime.now(), true);
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.singletonList(trade));
        Summary summary = new Summary();
        summary.setTotalValue(1000.0);
        var chart = service.createLineChartData("c1", summary);
        assertNotNull(chart);
        assertFalse(chart.getLabels().isEmpty());
        assertFalse(chart.getDatasets().isEmpty());
    }

    @Test
    @DisplayName("createPieChartData with holdings returns chart data")
    void testCreatePieChartDataWithHoldings() {
        Portfolio portfolio = new Portfolio("c1");
        PortfolioHolding holding = new PortfolioHolding(1L, "c1", "inst1", 10);
        portfolio.setHoldings(Set.of(holding));
        Instrument instrument = new Instrument("inst1", "desc", "type", "eid", "cat1", 1, 1);
        Price price = new Price("inst1", 100.0, 100.0, LocalDateTime.now());
        when(instrumentService.getInstrumentById("inst1")).thenReturn(Optional.of(instrument));
        when(priceService.getLatestPriceForInstrument("inst1")).thenReturn(Optional.of(price));
        var chart = service.createPieChartData(portfolio);
        assertNotNull(chart);
        assertFalse(chart.getLabels().isEmpty());
        assertFalse(chart.getDatasets().isEmpty());
    }

    @Test
    @DisplayName("createPieChartData throws when instrument missing")
    void testCreatePieChartDataWithMissingInstrumentThrows() {
        Portfolio portfolio = new Portfolio("c1");
        PortfolioHolding holding = new PortfolioHolding(1L, "c1", "inst1", 10);
        portfolio.setHoldings(Set.of(holding));
        when(instrumentService.getInstrumentById("inst1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createPieChartData(portfolio));
    }

    @Test
    @DisplayName("createPieChartData throws when price missing")
    void testCreatePieChartDataWithMissingPriceThrows() {
        Portfolio portfolio = new Portfolio("c1");
        PortfolioHolding holding = new PortfolioHolding(1L, "c1", "inst1", 10);
        portfolio.setHoldings(Set.of(holding));
        Instrument instrument = new Instrument("inst1", "desc", "type", "eid", "cat1", 1, 1);
        when(instrumentService.getInstrumentById("inst1")).thenReturn(Optional.of(instrument));
        when(priceService.getLatestPriceForInstrument("inst1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createPieChartData(portfolio));
    }

    @Test
    @DisplayName("createPerformanceColumns returns correct columns")
    void testCreatePerformanceColumns() {
        var cols = service.createPerformanceColumns();
        assertNotNull(cols);
        assertFalse(cols.isEmpty());
    }

    @Test
    @DisplayName("createPerformanceData with no trades returns empty list")
    void testCreatePerformanceDataWithNoTrades() {
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.emptyList());
        var data = service.createPerformanceData("c1", new Summary());
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("createPerformanceData with trades returns data")
    void testCreatePerformanceDataWithTrades() {
        Trade trade = new Trade("t1", "inst1", 10, 90.0, "BUY", "c1", "o1", 900.0, LocalDateTime.now(), true);
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.singletonList(trade));
        Summary summary = new Summary();
        summary.setTotalValue(1000.0);
        var data = service.createPerformanceData("c1", summary);
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    @DisplayName("createBarChartData returns chart data")
    void testCreateBarChartData() {
        var perf = new java.util.ArrayList<com.example.demo.dto.PerformanceData>();
        for (int i = 0; i < 3; i++) {
            var pd = new com.example.demo.dto.PerformanceData();
            pd.setDate("2025-09-08");
            pd.setGainLoss(i * 10.0);
            perf.add(pd);
        }
        var chart = service.createBarChartData(perf);
        assertNotNull(chart);
        assertEquals(3, chart.getLabels().size());
        assertEquals(1, chart.getDatasets().size());
    }

    @Test
    @DisplayName("createTradeHistoryColumns returns correct columns")
    void testCreateTradeHistoryColumns() {
        var cols = service.createTradeHistoryColumns();
        assertNotNull(cols);
        assertFalse(cols.isEmpty());
    }

    @Test
    @DisplayName("createTradeHistoryData with no trades returns empty list")
    void testCreateTradeHistoryDataWithNoTrades() {
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.emptyList());
        var data = service.createTradeHistoryData("c1");
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("createTradeHistoryData with trades returns data")
    void testCreateTradeHistoryDataWithTrades() {
        Trade trade = new Trade("t1", "inst1", 10, 100.0, "BUY", "c1", "o1", 1000.0, LocalDateTime.now(), true);
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.singletonList(trade));
        Instrument instrument = new Instrument("inst1", "desc", "type", "eid", "cat1", 1, 1);
        when(instrumentService.getInstrumentById("inst1")).thenReturn(Optional.of(instrument));
        var data = service.createTradeHistoryData("c1");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("desc", data.get(0).getAsset());
    }

    @Test
    @DisplayName("createTradeHistoryData throws when instrument missing")
    void testCreateTradeHistoryDataWithMissingInstrumentThrows() {
        Trade trade = new Trade("t1", "inst1", 10, 100.0, "BUY", "c1", "o1", 1000.0, LocalDateTime.now(), true);
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.singletonList(trade));
        when(instrumentService.getInstrumentById("inst1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createTradeHistoryData("c1"));
    }

    @Test
    @DisplayName("createColumn returns correct column definition")
    void testCreateColumn() {
        var col = service.createColumn("Header", "field");
        assertNotNull(col);
        assertEquals("Header", col.getHeaderName());
        assertEquals("field", col.getField());
    }
}
