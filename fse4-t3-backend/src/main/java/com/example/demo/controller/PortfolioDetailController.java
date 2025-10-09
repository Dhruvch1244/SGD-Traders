package com.example.demo.controller;

import com.example.demo.dto.portfolio.PortfolioPageDto;
import com.example.demo.service.PortfolioDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioDetailController {

    private final PortfolioDetailService portfolioDetailService;

    public PortfolioDetailController(PortfolioDetailService portfolioDetailService) {
        this.portfolioDetailService = portfolioDetailService;
    }

    @GetMapping("/{clientId}/details")
    public PortfolioPageDto getPortfolioDetails(@PathVariable String clientId) {
        return portfolioDetailService.getPortfolioDetails(clientId);
    }
}
