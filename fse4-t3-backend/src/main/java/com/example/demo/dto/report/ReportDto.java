package com.example.demo.dto.report;

import com.example.demo.dto.portfolio.PortfolioRowDto;
import com.example.demo.models.Trade;
import lombok.Data;

import java.util.List;

@Data
public class ReportDto {
    private String clientName;
    private double walletBalance;
    private List<PortfolioRowDto> portfolioHoldings;
    private List<Trade> tradeHistory;
}
