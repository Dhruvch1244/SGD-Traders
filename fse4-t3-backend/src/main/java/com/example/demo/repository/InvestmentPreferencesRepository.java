package com.example.demo.repository;

import com.example.demo.models.InvestmentPreferences;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface InvestmentPreferencesRepository {

    Optional<InvestmentPreferences> findByClientId(String clientId);

    void insert(InvestmentPreferences preferences);

    void update(InvestmentPreferences preferences);
}
