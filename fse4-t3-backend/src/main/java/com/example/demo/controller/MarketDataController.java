package com.example.demo.controller;

import com.example.demo.dto.market.MarketPerformerDto;
import com.example.demo.dto.market.MostActiveStockDto;
import com.example.demo.dto.market.TradePageInstrumentDto;
import com.example.demo.service.MarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/instruments")
    public List<TradePageInstrumentDto> getInstrumentsForTradePage(@RequestParam(required = false) String clientId) {
        return marketDataService.getInstrumentsForTradePage(clientId);
    }

    @GetMapping("/top-gainers")
    public List<MarketPerformerDto> getTopGainers() {
        return marketDataService.getTopGainers();
    }

    @GetMapping("/top-losers")
    public List<MarketPerformerDto> getTopLosers() {
        return marketDataService.getTopLosers();
    }

    @GetMapping("/most-active")
    public List<MostActiveStockDto> getMostActiveStocks() {
        return marketDataService.getMostActiveStocks();
    }
}
