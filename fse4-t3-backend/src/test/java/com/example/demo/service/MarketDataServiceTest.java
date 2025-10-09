package com.example.demo.service;

import com.example.demo.dto.market.MarketPerformerDto;
import com.example.demo.dto.market.MostActiveStockDto;
import com.example.demo.dto.market.TradePageInstrumentDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.*;
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
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataServiceTest {
    @Mock
    private InstrumentRepository instrumentRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private PortfolioRepository portfolioRepository;
    @InjectMocks
    private MarketDataService service;

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
    @DisplayName("getInstrumentsForTradePage returns empty list when no instruments exist")
    void testGetInstrumentsForTradePageWithNoInstruments() {
        when(instrumentRepository.findAll()).thenReturn(Collections.emptyList());
        List<TradePageInstrumentDto> result = service.getInstrumentsForTradePage("client123");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getInstrumentsForTradePage with null clientId")
    void testGetInstrumentsForTradePageWithNullClientId() {
        List<TradePageInstrumentDto> result = service.getInstrumentsForTradePage(null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getInstrumentsForTradePage marks instruments in portfolio")
    void testGetInstrumentsForTradePageMarksInstrumentsInPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setHoldings(Set.of(new PortfolioHolding(null, null, "inst1", 10)));
        when(portfolioRepository.findByClientId("client123")).thenReturn(Optional.of(portfolio));

        Instrument inst1 = new Instrument("inst1", "Instrument 1", null, null, null, 1, 100);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", null, null, null, 1, 100);
        when(instrumentRepository.findAll()).thenReturn(Arrays.asList(inst1, inst2));

        when(priceRepository.findLatestPrices()).thenReturn(Collections.emptyList());

        List<TradePageInstrumentDto> result = service.getInstrumentsForTradePage("client123");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst1") && dto.isInPortfolio()));
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst2") && !dto.isInPortfolio()));
    }

    @Test
    @DisplayName("getTopGainers returns empty list when no prices exist")
    void testGetTopGainersWithNoPrices() {
        when(priceRepository.findAll()).thenReturn(Collections.emptyList());
        List<MarketPerformerDto> result = service.getTopGainers();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTopLosers returns empty list when no prices exist")
    void testGetTopLosersWithNoPrices() {
        when(priceRepository.findAll()).thenReturn(Collections.emptyList());
        List<MarketPerformerDto> result = service.getTopLosers();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMostActiveStocks returns empty list when no trades exist")
    void testGetMostActiveStocksWithNoTrades() {
        when(tradeRepository.findAll()).thenReturn(Collections.emptyList());
        List<MostActiveStockDto> result = service.getMostActiveStocks();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTopGainers throws exception for missing instrument")
    void testGetTopGainersThrowsForMissingInstrument() {
        Price p1 = new Price("inst1", 100.0, 101.0, java.time.LocalDateTime.now().minusSeconds(100));
        Price p2 = new Price("inst1", 102.0, 103.0, java.time.LocalDateTime.now());
        when(priceRepository.findAll()).thenReturn(Arrays.asList(p1, p2));
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getTopGainers());
    }

    @Test
    @DisplayName("getMostActiveStocks throws exception for missing instrument")
    void testGetMostActiveStocksThrowsForMissingInstrument() {
        Trade trade = new Trade("trade1", "inst1", 10, 100.0, "BUY", "client1", null, 1000.0, java.time.LocalDateTime.now(), true);
        when(tradeRepository.findAll()).thenReturn(List.of(trade));
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMostActiveStocks());
    }

    @Test
    @DisplayName("getTopGainers returns correct performers for positive price change")
    void testGetTopGainersHappyPath() {
        Price p1_old = new Price("inst1", 100.0, 100.0, java.time.LocalDateTime.now().minusDays(2));
        Price p1_new = new Price("inst1", 110.0, 110.0, java.time.LocalDateTime.now().minusDays(1));
        Price p2_old = new Price("inst2", 200.0, 200.0, java.time.LocalDateTime.now().minusDays(2));
        Price p2_new = new Price("inst2", 220.0, 220.0, java.time.LocalDateTime.now().minusDays(1));
        when(priceRepository.findAll()).thenReturn(Arrays.asList(p1_old, p1_new, p2_old, p2_new));
        Instrument inst1 = new Instrument("inst1", "Instrument 1", null, null, null, 1, 100);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", null, null, null, 1, 100);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(inst1));
        when(instrumentRepository.findById("inst2")).thenReturn(Optional.of(inst2));
        List<MarketPerformerDto> result = service.getTopGainers();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst1") && dto.getPercentChange() == 10.0));
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst2") && dto.getPercentChange() == 10.0));
    }

    @Test
    @DisplayName("getTopLosers returns correct performers for negative price change")
    void testGetTopLosersHappyPath() {
        Price p1_old = new Price("inst1", 110.0, 110.0, java.time.LocalDateTime.now().minusDays(2));
        Price p1_new = new Price("inst1", 100.0, 100.0, java.time.LocalDateTime.now().minusDays(1));
        Price p2_old = new Price("inst2", 220.0, 220.0, java.time.LocalDateTime.now().minusDays(2));
        Price p2_new = new Price("inst2", 200.0, 200.0, java.time.LocalDateTime.now().minusDays(1));
        when(priceRepository.findAll()).thenReturn(Arrays.asList(p1_old, p1_new, p2_old, p2_new));
        Instrument inst1 = new Instrument("inst1", "Instrument 1", null, null, null, 1, 100);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", null, null, null, 1, 100);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(inst1));
        when(instrumentRepository.findById("inst2")).thenReturn(Optional.of(inst2));
        List<MarketPerformerDto> result = service.getTopLosers();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst1") && Math.abs(dto.getPercentChange() + 9.09) < 0.01));
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst2") && Math.abs(dto.getPercentChange() + 9.09) < 0.01));
    }

    @Test
    @DisplayName("getMostActiveStocks returns correct DTOs for multiple trades")
    void testGetMostActiveStocksHappyPath() {
        Trade trade1 = new Trade("trade1", "inst1", 10, 100.0, "BUY", "client1", null, 1000.0, java.time.LocalDateTime.now(), true);
        Trade trade2 = new Trade("trade2", "inst2", 20, 200.0, "SELL", "client2", null, 4000.0, java.time.LocalDateTime.now(), true);
        when(tradeRepository.findAll()).thenReturn(Arrays.asList(trade1, trade2));
        Instrument inst1 = new Instrument("inst1", "Instrument 1", null, null, null, 1, 100);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", null, null, null, 1, 100);
        when(instrumentRepository.findById("inst1")).thenReturn(Optional.of(inst1));
        when(instrumentRepository.findById("inst2")).thenReturn(Optional.of(inst2));
        List<MostActiveStockDto> result = service.getMostActiveStocks();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst1") && dto.getTotalVolume() == 10));
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst2") && dto.getTotalVolume() == 20));
    }

    @Test
    @DisplayName("getInstrumentsForTradePage returns correct DTOs for multiple instruments and portfolio")
    void testGetInstrumentsForTradePageHappyPath() {
        Portfolio portfolio = new Portfolio();
        portfolio.setHoldings(Set.of(new PortfolioHolding(null, null, "inst1", 10)));
        when(portfolioRepository.findByClientId("client123")).thenReturn(Optional.of(portfolio));
        Instrument inst1 = new Instrument("inst1", "Instrument 1", null, null, null, 1, 100);
        Instrument inst2 = new Instrument("inst2", "Instrument 2", null, null, null, 1, 100);
        when(instrumentRepository.findAll()).thenReturn(Arrays.asList(inst1, inst2));
        Price price1 = new Price("inst1", 100.0, 110.0, java.time.LocalDateTime.now());
        Price price2 = new Price("inst2", 200.0, 210.0, java.time.LocalDateTime.now());
        when(priceRepository.findLatestPrices()).thenReturn(Arrays.asList(price1, price2));
        List<TradePageInstrumentDto> result = service.getInstrumentsForTradePage("client123");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst1") && dto.isInPortfolio() && dto.getAskPrice() == 110.0));
        assertTrue(result.stream().anyMatch(dto -> dto.getInstrumentId().equals("inst2") && !dto.isInPortfolio() && dto.getAskPrice() == 210.0));
    }
}
