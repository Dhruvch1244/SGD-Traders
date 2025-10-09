package com.example.demo.service;

import com.example.demo.models.Price;
import com.example.demo.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceServiceTest {
    @Mock
    private PriceRepository priceRepository;
    @InjectMocks
    private PriceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PriceService(priceRepository);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testServiceInstantiation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("getLatestPriceForInstrument returns empty for unknown instrumentId (Negative)")
    void testGetLatestPriceForInstrumentReturnsEmptyForUnknownId() {
        when(priceRepository.findLatestPriceForInstrument("unknown")).thenReturn(Optional.empty());
        Optional<Price> result = service.getLatestPriceForInstrument("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllPrices returns empty list (Border Condition)")
    void testGetAllPricesReturnsEmptyList() {
        when(priceRepository.findAll()).thenReturn(Collections.emptyList());
        List<Price> result = service.getAllPrices();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLatestPriceForInstrument with long instrumentId (Extreme Limits)")
    void testGetLatestPriceForInstrumentWithExtremeId() {
        String longId = "I".repeat(1000);
        when(priceRepository.findLatestPriceForInstrument(longId)).thenReturn(Optional.empty());
        Optional<Price> result = service.getLatestPriceForInstrument(longId);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLatestPriceForInstrument returns correct Price (Happy Path)")
    void testGetLatestPriceForInstrumentHappyPath() {
        Price price = new Price();
        price.setInstrumentId("id123");
        price.setAskPrice(123.45);
        when(priceRepository.findLatestPriceForInstrument("id123")).thenReturn(Optional.of(price));
        Optional<Price> result = service.getLatestPriceForInstrument("id123");
        assertTrue(result.isPresent());
        assertEquals("id123", result.get().getInstrumentId());
        assertEquals(123.45, result.get().getAskPrice());
    }
}
