package com.example.demo.controller;

import com.example.demo.dto.InstrumentDto;
import com.example.demo.models.Trade;
import com.example.demo.service.InstrumentService;
import com.example.demo.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    @Autowired
    InstrumentService instrumentService;

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/{clientId}")
    public List<Trade> getTradeHistory(@PathVariable String clientId) {
        return tradeService.getTradeHistory(clientId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InstrumentDto>> searchInstruments(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category) {
        List<InstrumentDto> instruments = instrumentService.searchInstruments(description, category);
        return ResponseEntity.ok(instruments);
    }
}
