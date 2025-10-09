package com.example.demo.service;

import com.example.demo.models.Wallet;
import com.example.demo.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Optional<Wallet> getWalletByClientId(String clientId) {
        return walletRepository.findByClientId(clientId);
    }

    @Transactional
    public Optional<Wallet> addMoney(String clientId, double amount) {
        Optional<Wallet> walletOpt = walletRepository.findByClientId(clientId);
        if (walletOpt.isPresent()) {
            Wallet wallet = walletOpt.get();
            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.update(wallet);
            return Optional.of(wallet);
        } else {
            return Optional.empty();
        }
    }
}
