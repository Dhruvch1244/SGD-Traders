package com.example.demo.dto.portfolio;

import com.example.demo.dto.ChartData;
import lombok.Data;

import java.util.List;

@Data
public class PortfolioPageDto {
    private List<PortfolioRowDto> rowData;
    private ChartData categoryAllocationChart;
    private ChartData instrumentAllocationChart;
}
