package com.example.demo.service;

import com.example.demo.dto.portfolio.PortfolioPageDto;
import com.example.demo.dto.portfolio.PortfolioRowDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.Instrument;
import com.example.demo.models.Portfolio;
import com.example.demo.models.PortfolioHolding;
import com.example.demo.models.Price;
import com.example.demo.repository.InstrumentRepository;
import com.example.demo.repository.PortfolioRepository;
import com.example.demo.repository.PriceRepository;
import com.example.demo.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PortfolioDetailServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private InstrumentRepository instrumentRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private TradeRepository tradeRepository;
    @InjectMocks
    private PortfolioDetailService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getPortfolioDetails throws ResourceNotFoundException when portfolio not found")
    void testGetPortfolioDetails_throwsResourceNotFoundException_whenPortfolioNotFound() {
        when(portfolioRepository.findByClientId("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getPortfolioDetails("unknown"));
    }

    @Test
    @DisplayName("createPortfolioRowData throws ResourceNotFoundException when instrument not found")
    void testCreatePortfolioRowData_throwsResourceNotFoundException_whenInstrumentNotFound() {
        Portfolio portfolio = new Portfolio("c1");
        // holdings set via repository mock
        Set<PortfolioHolding> holdings = Set.of(new PortfolioHolding(1L, "c1", "inst1", 10));
        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId("c1")).thenReturn(holdings);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createPortfolioRowData("c1"));
    }

    @Test
    @DisplayName("createPortfolioRowData throws ResourceNotFoundException when price not found")
    void testCreatePortfolioRowData_throwsResourceNotFoundException_whenPriceNotFound() {
        Portfolio portfolio = new Portfolio("c1");
        Set<PortfolioHolding> holdings = Set.of(new PortfolioHolding(1L, "c1", "inst1", 10));
        Instrument instrument = new Instrument("inst1", "Test Instrument", "STOCK", "TICK", "CAT1", 1, 1000);

        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId("c1")).thenReturn(holdings);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPrices()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> service.createPortfolioRowData("c1"));
    }

    @Test
    @DisplayName("createPortfolioRowData returns empty list when no holdings")
    void testCreatePortfolioRowData_returnsEmptyList_whenNoHoldings() {
        Portfolio portfolio = new Portfolio("c1");
        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId("c1")).thenReturn(Collections.emptySet());

        List<PortfolioRowDto> result = service.createPortfolioRowData("c1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createPortfolioRowData calculates row data correctly")
    void testCreatePortfolioRowData_calculatesRowDataCorrectly() {
        Portfolio portfolio = new Portfolio("c1");
        Set<PortfolioHolding> holdings = Set.of(new PortfolioHolding(1L, "c1", "inst1", 10));
        Instrument instrument = new Instrument("inst1", "Test Instrument", "STOCK", "TICK", "CAT1", 1, 1000);
        Price price = new Price("inst1", 100.0, 100.0, LocalDateTime.now());

        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId("c1")).thenReturn(holdings);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(instrument));
        when(priceRepository.findLatestPrices()).thenReturn(Collections.singletonList(price));
        when(tradeRepository.findAll()).thenReturn(Collections.emptyList());
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.emptyList());

        List<PortfolioRowDto> result = service.createPortfolioRowData("c1");

        assertEquals(1, result.size());
        PortfolioRowDto row = result.get(0);
        assertEquals("inst1", row.getInstrumentId());
        assertEquals("Test Instrument", row.getInstrumentName());
        assertEquals(10, row.getQuantity());
        assertEquals(1000.0, row.getMarketValue());
    }

    @Test
    @DisplayName("getPortfolioDetails returns DTO with chart data")
    void testGetPortfolioDetails_returnsDtoWithCharts() {
        Portfolio portfolio = new Portfolio("c1");
        Set<PortfolioHolding> holdings = Set.of(
                new PortfolioHolding(1L, "c1", "inst1", 10),
                new PortfolioHolding(2L, "c1", "inst2", 20)
        );
        Instrument inst1 = new Instrument("inst1", "Instrument 1", "STOCK", "TICK1", "CAT1", 1, 1000);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", "BOND", "TICK2", "CAT2", 1, 1000);
        Price price1 = new Price("inst1", 10.0, 10.0, LocalDateTime.now());
        Price price2 = new Price("inst2", 20.0, 20.0, LocalDateTime.now());

        when(portfolioRepository.findByClientId("c1")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findHoldingsByPortfolioId("c1")).thenReturn(holdings);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(inst1));
        when(instrumentRepository.findById("inst2")).thenReturn(Optional.of(inst2));
        when(priceRepository.findLatestPrices()).thenReturn(Arrays.asList(price1, price2));
        when(tradeRepository.findByClientId("c1")).thenReturn(Collections.emptyList());

        PortfolioPageDto result = service.getPortfolioDetails("c1");

        assertNotNull(result);
        assertNotNull(result.getCategoryAllocationChart());
        assertNotNull(result.getInstrumentAllocationChart());
        assertFalse(result.getCategoryAllocationChart().getDatasets().get(0).getData().isEmpty());
        assertFalse(result.getInstrumentAllocationChart().getDatasets().get(0).getData().isEmpty());
    }
}
