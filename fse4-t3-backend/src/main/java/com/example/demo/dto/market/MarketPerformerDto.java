package com.example.demo.dto.market;

import lombok.Data;

@Data
public class MarketPerformerDto {
    private String instrumentId;
    private String description;
    private double priceChange;
    private double percentChange;
    private double currentPrice;
}
