package com.example.demo.dto.market;

import lombok.Data;

@Data
public class TradePageInstrumentDto {
    private String instrumentId;
    private String description;
    private int minQuantity;
    private int maxQuantity;
    private double askPrice;
    private double bidPrice;
    private boolean isInPortfolio;
}
