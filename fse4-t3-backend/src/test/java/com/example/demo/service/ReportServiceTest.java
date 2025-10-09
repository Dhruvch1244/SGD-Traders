package com.example.demo.service;

import com.example.demo.dto.report.ReportDto;
import com.example.demo.models.Client;
import com.example.demo.models.Wallet;
import com.example.demo.models.Trade;
import com.example.demo.models.report.TimeScale;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.TradeRepository;
import com.example.demo.repository.WalletRepository;
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

class ReportServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private PortfolioDetailService portfolioDetailService;
    @InjectMocks
    private ReportService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReportService(clientRepository, walletRepository, tradeRepository, portfolioDetailService);
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testServiceInstantiation() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("generateReport throws for unknown clientId (Negative)")
    void testGenerateReportThrowsForUnknownClientId() {
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());
        when(walletRepository.findByClientId("unknown")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.generateReport("unknown", TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport with empty clientId (Border Condition)")
    void testGenerateReportWithEmptyClientId() {
        when(clientRepository.findById("")).thenReturn(Optional.empty());
        when(walletRepository.findByClientId("")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.generateReport("", TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport with long clientId (Extreme Limits)")
    void testGenerateReportWithExtremeClientId() {
        String longId = "C".repeat(1000);
        when(clientRepository.findById(longId)).thenReturn(Optional.empty());
        when(walletRepository.findByClientId(longId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.generateReport(longId, TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport throws for null clientId")
    void testGenerateReportNullClientId() {
        assertThrows(IllegalArgumentException.class, () -> service.generateReport(null, TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport throws for null TimeScale")
    void testGenerateReportNullTimeScale() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        assertThrows(NullPointerException.class, () -> service.generateReport("client123", null));
    }

    @Test
    @DisplayName("generateReport throws for client found but wallet not found")
    void testGenerateReportClientFoundWalletNotFound() {
        Client client = new Client();
        client.setName("John Doe");
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.generateReport("client123", TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport throws for wallet found but client not found")
    void testGenerateReportWalletFoundClientNotFound() {
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        when(clientRepository.findById("client123")).thenReturn(Optional.empty());
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        assertThrows(IllegalArgumentException.class, () -> service.generateReport("client123", TimeScale.ALL_TIME));
    }

    @Test
    @DisplayName("generateReport returns populated ReportDto (Happy Path)")
    void testGenerateReportHappyPath() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = List.of(new Trade());
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientId("client123")).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn(Collections.emptyList());
        ReportDto result = service.generateReport("client123", TimeScale.ALL_TIME);
        assertEquals("John Doe", result.getClientName());
        assertEquals(1000.0, result.getWalletBalance());
        assertEquals(trades, result.getTradeHistory());
    }

    @Test
    @DisplayName("generateReport returns correct trades for ONE_MONTH")
    void testGenerateReportOneMonth() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = List.of(new Trade());
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientIdAndTimestampAfter(eq("client123"), any())).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn(Collections.emptyList());
        ReportDto result = service.generateReport("client123", TimeScale.ONE_MONTH);
        assertEquals(trades, result.getTradeHistory());
    }

    @Test
    @DisplayName("generateReport returns correct trades for SIX_MONTHS")
    void testGenerateReportSixMonths() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = List.of(new Trade());
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientIdAndTimestampAfter(eq("client123"), any())).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn(Collections.emptyList());
        ReportDto result = service.generateReport("client123", TimeScale.SIX_MONTHS);
        assertEquals(trades, result.getTradeHistory());
    }

    @Test
    @DisplayName("generateReport returns correct trades for ONE_YEAR")
    void testGenerateReportOneYear() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = List.of(new Trade());
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientIdAndTimestampAfter(eq("client123"), any())).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn(Collections.emptyList());
        ReportDto result = service.generateReport("client123", TimeScale.ONE_YEAR);
        assertEquals(trades, result.getTradeHistory());
    }

    @Test
    @DisplayName("generateReport returns correct trades for ALL_TIME with empty trades")
    void testGenerateReportAllTimeEmptyTrades() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = Collections.emptyList();
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientId("client123")).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn(Collections.emptyList());
        ReportDto result = service.generateReport("client123", TimeScale.ALL_TIME);
        assertEquals(trades, result.getTradeHistory());
    }

    @Test
    @DisplayName("generateReport returns non-empty portfolio holdings")
    void testGenerateReportNonEmptyPortfolioHoldings() {
        Client client = new Client();
        client.setName("John Doe");
        Wallet wallet = new Wallet();
        wallet.setBalance(1000.0);
        List<Trade> trades = List.of(new Trade());
        List<Object> holdings = List.of(new Object());
        when(clientRepository.findById("client123")).thenReturn(Optional.of(client));
        when(walletRepository.findByClientId("client123")).thenReturn(Optional.of(wallet));
        when(tradeRepository.findByClientId("client123")).thenReturn(trades);
        when(portfolioDetailService.createPortfolioRowData("client123")).thenReturn((List) holdings);
        ReportDto result = service.generateReport("client123", TimeScale.ALL_TIME);
        assertEquals(holdings, result.getPortfolioHoldings());
    }
}
