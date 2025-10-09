package com.example.demo.dto.portfolio;

import lombok.Data;

@Data
public class PortfolioRowDto {
    private String instrumentId;
    private String instrumentName;
    private int quantity;
    private String categoryId;
    private double marketValue;
    private double costBasis;
    private double totalCost;
    private double unrealizedPl;
    private double unrealizedPlPercent;
}
