package com.example.demo.service;

import com.example.demo.models.Trade;
import com.example.demo.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> getTradeHistory(String clientId) {
        return tradeRepository.findByClientId(clientId);
    }
}
