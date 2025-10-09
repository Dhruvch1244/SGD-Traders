package com.example.demo.repository;

import com.example.demo.models.Wallet;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WalletRepository {
    Optional<Wallet> findByClientId(String clientId);

    void insert(Wallet wallet);

    void update(Wallet wallet);
}
