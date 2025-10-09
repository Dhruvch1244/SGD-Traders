package com.example.demo.service;

import com.example.demo.models.Trade;
import com.example.demo.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TradeServiceTest {
    @Mock
    private TradeRepository tradeRepository;
    @InjectMocks
    private TradeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TradeService(tradeRepository);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testServiceInstantiation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("getTradeHistory returns empty for unknown clientId (Negative)")
    void testGetTradeHistoryReturnsEmptyForUnknownClientId() {
        when(tradeRepository.findByClientId("unknown")).thenReturn(Collections.emptyList());
        List<Trade> result = service.getTradeHistory("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTradeHistory with empty clientId (Border Condition)")
    void testGetTradeHistoryWithEmptyClientId() {
        when(tradeRepository.findByClientId("")).thenReturn(Collections.emptyList());
        List<Trade> result = service.getTradeHistory("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTradeHistory with long clientId (Extreme Limits)")
    void testGetTradeHistoryWithExtremeClientId() {
        String longId = "C".repeat(1000);
        when(tradeRepository.findByClientId(longId)).thenReturn(Collections.emptyList());
        List<Trade> result = service.getTradeHistory(longId);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTradeHistory returns populated list (Happy Path)")
    void testGetTradeHistoryHappyPath() {
        Trade trade = new Trade();
        trade.setClientId("client123");
        trade.setTradeId("trade1");
        List<Trade> trades = List.of(trade);
        when(tradeRepository.findByClientId("client123")).thenReturn(trades);
        List<Trade> result = service.getTradeHistory("client123");
        assertEquals(1, result.size());
        assertEquals("client123", result.get(0).getClientId());
        assertEquals("trade1", result.get(0).getTradeId());
    }
}
