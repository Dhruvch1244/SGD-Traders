package com.example.demo.repository;

import com.example.demo.models.Trade;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TradeRepository {
    List<Trade> findAll();

    List<Trade> findByClientId(String clientId);

    List<Trade> findByClientIdAndTimestampAfter(String clientId, LocalDateTime timestamp);

    void insert(Trade trade);
}
