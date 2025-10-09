package com.example.demo.service;

import com.example.demo.dto.report.ReportDto;
import com.example.demo.models.Client;
import com.example.demo.models.Trade;
import com.example.demo.models.Wallet;
import com.example.demo.models.report.TimeScale;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.TradeRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportService {

    private final ClientRepository clientRepository;
    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioDetailService portfolioDetailService;

    public ReportService(ClientRepository clientRepository, WalletRepository walletRepository,
                         TradeRepository tradeRepository, PortfolioDetailService portfolioDetailService) {
        this.clientRepository = clientRepository;
        this.walletRepository = walletRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioDetailService = portfolioDetailService;
    }

    public ReportDto generateReport(String clientId, TimeScale timeScale) {
        ReportDto reportDto = new ReportDto();

        Client client = getClient(clientId);
        Wallet wallet = getWallet(clientId);

        reportDto.setClientName(client.getName());
        reportDto.setWalletBalance(wallet.getBalance());
        reportDto.setPortfolioHoldings(portfolioDetailService.createPortfolioRowData(clientId));
        reportDto.setTradeHistory(filterTradesByTimeScale(clientId, timeScale));

        return reportDto;
    }

    private List<Trade> filterTradesByTimeScale(String clientId, TimeScale timeScale) {
        if (timeScale == TimeScale.ALL_TIME) {
            return tradeRepository.findByClientId(clientId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = getStartTime(now, timeScale);

        return tradeRepository.findByClientIdAndTimestampAfter(clientId, startTime);
    }

    private LocalDateTime getStartTime(LocalDateTime now, TimeScale timeScale) {
        switch (timeScale) {
            case ONE_MONTH: return now.minus(30, ChronoUnit.DAYS);
            case SIX_MONTHS: return now.minus(180, ChronoUnit.DAYS);
            case ONE_YEAR: return now.minus(365, ChronoUnit.DAYS);
            default: return LocalDateTime.MIN;
        }
    }

    private Client getClient(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
    }

    private Wallet getWallet(String clientId) {
        return walletRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for client: " + clientId));
    }
}
