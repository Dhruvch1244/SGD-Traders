package com.example.demo.dto;

import java.util.List;

public class PortfolioSummary {
    private Summary portfolioSummary;
    private ChartData lineChartData;
    private ChartData pieChartData;
    private List<ColumnDefinition> performanceColumns;
    private List<PerformanceData> performanceData;
    private ChartData barChartData;
    private List<ColumnDefinition> tradeHistoryColumns;
    private List<TradeHistoryData> tradeHistoryData;

    public PortfolioSummary() {}

    public Summary getPortfolioSummary() { return portfolioSummary; }
    public void setPortfolioSummary(Summary portfolioSummary) { this.portfolioSummary = portfolioSummary; }

    public ChartData getLineChartData() { return lineChartData; }
    public void setLineChartData(ChartData lineChartData) { this.lineChartData = lineChartData; }

    public ChartData getPieChartData() { return pieChartData; }
    public void setPieChartData(ChartData pieChartData) { this.pieChartData = pieChartData; }

    public List<ColumnDefinition> getPerformanceColumns() { return performanceColumns; }
    public void setPerformanceColumns(List<ColumnDefinition> performanceColumns) { this.performanceColumns = performanceColumns; }

    public List<PerformanceData> getPerformanceData() { return performanceData; }
    public void setPerformanceData(List<PerformanceData> performanceData) { this.performanceData = performanceData; }

    public ChartData getBarChartData() { return barChartData; }
    public void setBarChartData(ChartData barChartData) { this.barChartData = barChartData; }

    public List<ColumnDefinition> getTradeHistoryColumns() { return tradeHistoryColumns; }
    public void setTradeHistoryColumns(List<ColumnDefinition> tradeHistoryColumns) { this.tradeHistoryColumns = tradeHistoryColumns; }

    public List<TradeHistoryData> getTradeHistoryData() { return tradeHistoryData; }
    public void setTradeHistoryData(List<TradeHistoryData> tradeHistoryData) { this.tradeHistoryData = tradeHistoryData; }
}
