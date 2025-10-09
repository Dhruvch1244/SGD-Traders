package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.wallet.AddMoneyRequestDto;
import com.example.demo.models.Wallet;
import com.example.demo.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Wallet> getWalletByClientId(@PathVariable String clientId) {
        Optional<Wallet> wallet = walletService.getWalletByClientId(clientId);
        return wallet.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{clientId}/add")
    public ResponseEntity<ApiResponse> addMoney(@PathVariable String clientId, @Valid @RequestBody AddMoneyRequestDto request) {
        try {
            return walletService.addMoney(clientId, request.getAmount())
                    .map(updatedWallet -> new ApiResponse(true, "Funds added successfully", updatedWallet))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> new ResponseEntity<>(new ApiResponse(false, "Wallet not found"), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to add funds to wallet"));
        }
    }
}
