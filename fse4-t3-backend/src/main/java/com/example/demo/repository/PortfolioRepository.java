package com.example.demo.repository;

import com.example.demo.models.Portfolio;
import com.example.demo.models.PortfolioHolding;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;
import java.util.Set;

@Mapper
public interface PortfolioRepository {
    Optional<Portfolio> findByClientId(String clientId);

    Set<PortfolioHolding> findHoldingsByPortfolioId(String portfolioId);

    void insert(Portfolio portfolio);

    void deleteHoldings(String portfolioId);

    void insertHolding(PortfolioHolding holding);

    Optional<PortfolioHolding> findHolding(String portfolioId, String instrumentId);

    void updateHolding(PortfolioHolding holding);

    void deleteHolding(String portfolioId, String instrumentId);
}
