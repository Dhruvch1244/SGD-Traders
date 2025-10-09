package com.example.demo.dto;


public class TradeHistoryData {
    private String date;
    private String asset;
    private String type;
    private double amount;
    private double price;

    public TradeHistoryData() {}

    public TradeHistoryData(String date, String asset, String type, double amount, double price) {
        this.date = date;
        this.asset = asset;
        this.type = type;
        this.amount = amount;
        this.price = price;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
