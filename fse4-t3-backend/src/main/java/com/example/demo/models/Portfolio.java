package com.example.demo.models;

// Lombok removed; explicit methods below

import java.util.HashSet;
import java.util.Set;

public class Portfolio {
    private String clientId;
    private Set<PortfolioHolding> holdings = new HashSet<>();

    public Portfolio() {}

    public Portfolio(String clientId) {
        this.clientId = clientId;
        this.holdings = new HashSet<>();
    }

    public Portfolio(String clientId, Set<PortfolioHolding> holdings) {
        this.clientId = clientId;
        this.holdings = holdings;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Set<PortfolioHolding> getHoldings() { return holdings; }
    public void setHoldings(Set<PortfolioHolding> holdings) { this.holdings = holdings; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio that = (Portfolio) o;
        return java.util.Objects.equals(clientId, that.clientId) &&
                java.util.Objects.equals(holdings, that.holdings);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(clientId, holdings);
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "clientId='" + clientId + '\'' +
                ", holdings=" + holdings +
                '}';
    }
}
