package com.example.demo.dto.market;

import lombok.Data;

@Data
public class MostActiveStockDto {
    private String instrumentId;
    private String description;
    private long totalVolume;
}
