package com.example.demo.service;

import com.example.demo.models.Instrument;
import com.example.demo.models.Price;
import com.example.demo.models.Wallet;
import com.example.demo.repository.InstrumentRepository;
import com.example.demo.repository.PriceRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletPriceInstrumentServiceTest {

    @Mock WalletRepository walletRepository;
    @Mock PriceRepository priceRepository;
    @Mock InstrumentRepository instrumentRepository;

    @InjectMocks WalletService walletService;
    @InjectMocks PriceService priceService;
    @InjectMocks InstrumentService instrumentService;

    @Test
    void wallet_addMoney_updatesBalance() {
        Wallet w = new Wallet("C1", 10.0);
        when(walletRepository.findByClientId("C1")).thenReturn(Optional.of(w));
        var updated = walletService.addMoney("C1", 5.0);
        assertTrue(updated.isPresent());
        assertEquals(15.0, updated.get().getBalance(), 0.0001);
        verify(walletRepository).update(any());
    }

    @Test
    void price_latest_forInstrument_returnsValue() {
        Price p = new Price("I1", 1.0, 2.0, LocalDateTime.now());
        when(priceRepository.findLatestPriceForInstrument("I1")).thenReturn(Optional.of(p));
        assertTrue(priceService.getLatestPriceForInstrument("I1").isPresent());
    }

    @Test
    void price_getAll_returnsList() {
        when(priceRepository.findAll()).thenReturn(List.of(new Price()));
        assertFalse(priceService.getAllPrices().isEmpty());
    }

    @Test
    void instrument_findAll_returnsList() {
        when(instrumentRepository.findAll()).thenReturn(List.of(new Instrument()));
        assertFalse(instrumentService.getAllInstruments().isEmpty());
    }

    @Test
    void instrument_search_mapsToDto() {
        when(instrumentRepository.findByCriteria(any(), any())).thenReturn(List.of(new Instrument("I1","Desc","T","E","CAT",1,10)));
        var result = instrumentService.searchInstruments("Desc","CAT");
        assertEquals(1, result.size());
        assertEquals("I1", result.get(0).getInstrumentId());
    }
}


