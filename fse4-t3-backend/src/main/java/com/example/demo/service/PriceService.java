package com.example.demo.service;

import com.example.demo.models.Price;
import com.example.demo.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public List<Price> getAllPrices() {
        return priceRepository.findAll();
    }

    public Optional<Price> getLatestPriceForInstrument(String instrumentId) {
        return priceRepository.findLatestPriceForInstrument(instrumentId);
    }
}
