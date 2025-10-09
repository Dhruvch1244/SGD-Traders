package com.example.demo.controller;

import com.example.demo.models.Price;
import com.example.demo.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final PriceService priceService;

    public MarketController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/instruments/{instrumentId}/price")
    public ResponseEntity<Price> getLatestInstrumentPrice(@PathVariable String instrumentId) {
        return priceService.getLatestPriceForInstrument(instrumentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}