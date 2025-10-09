package com.example.demo.service;

import com.example.demo.models.Wallet;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;
    @InjectMocks
    private WalletService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WalletService(walletRepository);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testServiceInstantiation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("getWalletByClientId returns empty for unknown clientId (Negative)")
    void testGetWalletByClientIdReturnsEmptyForUnknownClientId() {
        when(walletRepository.findByClientId("unknown")).thenReturn(Optional.empty());
        Optional<Wallet> result = service.getWalletByClientId("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("addMoney returns empty for unknown clientId (Border Condition)")
    void testAddMoneyReturnsEmptyForUnknownClientId() {
        when(walletRepository.findByClientId("unknown")).thenReturn(Optional.empty());
        Optional<Wallet> result = service.addMoney("unknown", 100.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("addMoney with maximal amount (Extreme Limits)")
    void testAddMoneyWithExtremeAmount() {
        Wallet wallet = new Wallet();
        wallet.setBalance(Double.MAX_VALUE - 1);
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        doNothing().when(walletRepository).update(any(Wallet.class));
        Optional<Wallet> result = service.addMoney("client123", 1.0);
        assertTrue(result.isPresent());
        assertEquals(Double.MAX_VALUE, result.get().getBalance());
    }

    @Test
    @DisplayName("addMoney returns updated wallet (Happy Path)")
    void testAddMoneyHappyPath() {
        Wallet wallet = new Wallet();
        wallet.setBalance(100.0);
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        doNothing().when(walletRepository).update(any(Wallet.class));
        Optional<Wallet> result = service.addMoney("client123", 50.0);
        assertTrue(result.isPresent());
        assertEquals(150.0, result.get().getBalance());
    }
}
