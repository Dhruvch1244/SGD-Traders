package com.example.demo.service;

import com.example.demo.dto.InstrumentDto;
import com.example.demo.models.Instrument;
import com.example.demo.repository.InstrumentRepository;
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

class InstrumentServiceTest {
    @Mock
    private InstrumentRepository instrumentRepository;
    @InjectMocks
    private InstrumentService instrumentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instrumentService = new InstrumentService(instrumentRepository);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testInstrumentServiceInstantiation() {
        assertNotNull(instrumentService);
    }

    @Test
    @DisplayName("getInstrumentById returns empty for unknown ID (Negative)")
    void testGetInstrumentByIdReturnsEmptyForUnknownId() {
        when(instrumentRepository.findById("unknown")).thenReturn(Optional.empty());
        Optional<Instrument> result = instrumentService.getInstrumentById("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllInstruments returns empty list (Border Condition)")
    void testGetAllInstrumentsReturnsEmptyList() {
        when(instrumentRepository.findAll()).thenReturn(Collections.emptyList());
        List<Instrument> result = instrumentService.getAllInstruments();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("searchInstruments with long description and category (Extreme Limits)")
    void testSearchInstrumentsWithExtremeLimits() {
        String longDesc = "D".repeat(1000);
        String longCat = "C".repeat(1000);
        when(instrumentRepository.findByCriteria(longDesc, longCat)).thenReturn(Collections.emptyList());
        List<InstrumentDto> result = instrumentService.searchInstruments(longDesc, longCat);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getInstrumentById returns correct instrument (Happy Path)")
    void testGetInstrumentByIdReturnsInstrument() {
        Instrument instrument = new Instrument();
        instrument.setInstrumentId("id123");
        instrument.setDescription("desc");
        instrument.setCategoryId("cat1");
        when(instrumentRepository.findById("id123")).thenReturn(Optional.of(instrument));
        Optional<Instrument> result = instrumentService.getInstrumentById("id123");
        assertTrue(result.isPresent());
        assertEquals("id123", result.get().getInstrumentId());
        assertEquals("desc", result.get().getDescription());
        assertEquals("cat1", result.get().getCategoryId());
    }
}
