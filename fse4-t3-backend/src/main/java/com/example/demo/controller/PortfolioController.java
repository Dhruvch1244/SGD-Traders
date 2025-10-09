package com.example.demo.controller;

import com.example.demo.dto.PortfolioSummary;
import com.example.demo.dto.portfolio.PortfolioPageDto;
import com.example.demo.models.Portfolio;
import com.example.demo.service.PortfolioDetailService;
import com.example.demo.service.PortfolioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioDetailService portfolioDetailService;

    public PortfolioController(PortfolioService portfolioService, PortfolioDetailService portfolioDetailService) {
        this.portfolioService = portfolioService;
        this.portfolioDetailService = portfolioDetailService;
    }

    @GetMapping("/{clientId}")
    public Portfolio getPortfolioByClientId(@PathVariable String clientId) {
        return portfolioService.getPortfolioByClientId(clientId);
    }

    @GetMapping("/{clientId}/summary")
    public PortfolioSummary getPortfolioSummary(@PathVariable String clientId) {
        return portfolioService.getPortfolioSummary(clientId);
    }

    @GetMapping("/{clientId}/details")
    public PortfolioPageDto getPortfolioDetails(@PathVariable String clientId) {
        return portfolioDetailService.getPortfolioDetails(clientId);
    }
}
