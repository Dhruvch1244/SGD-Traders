package com.example.demo.dto;


public class PerformanceData {
    private String date;
    private double gainLoss;
    private int volume;

    public PerformanceData() {}

    public PerformanceData(String date, double gainLoss, int volume) {
        this.date = date;
        this.gainLoss = gainLoss;
        this.volume = volume;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getGainLoss() { return gainLoss; }
    public void setGainLoss(double gainLoss) { this.gainLoss = gainLoss; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }
}
