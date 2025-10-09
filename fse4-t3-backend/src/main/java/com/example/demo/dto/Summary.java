package com.example.demo.dto;


public class Summary {
    private double totalValue;
    private double totalGainLoss;

    public Summary() {}

    public Summary(double totalValue, double totalGainLoss) {
        this.totalValue = totalValue;
        this.totalGainLoss = totalGainLoss;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getTotalGainLoss() {
        return totalGainLoss;
    }

    public void setTotalGainLoss(double totalGainLoss) {
        this.totalGainLoss = totalGainLoss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Summary summary = (Summary) o;
        return Double.compare(summary.totalValue, totalValue) == 0 &&
                Double.compare(summary.totalGainLoss, totalGainLoss) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(totalValue, totalGainLoss);
    }

    @Override
    public String toString() {
        return "Summary{" +
                "totalValue=" + totalValue +
                ", totalGainLoss=" + totalGainLoss +
                '}';
    }
}
