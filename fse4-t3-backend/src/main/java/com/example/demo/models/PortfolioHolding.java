package com.example.demo.models;

// Lombok removed; explicit methods below
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("PORTFOLIO_HOLDINGS")
public class PortfolioHolding {
    @Id
    @Column("HOLDINGID")
    private Long holdingId;
    @Column("PORTFOLIOID")
    private String portfolioId;
    @Column("INSTRUMENTID")
    private String instrumentId;
    @Column("QUANTITY")
    private int quantity;

    public PortfolioHolding() {}

    public PortfolioHolding(Long holdingId, String portfolioId, String instrumentId, int quantity) {
        this.holdingId = holdingId;
        this.portfolioId = portfolioId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
    }

    public Long getHoldingId() { return holdingId; }
    public void setHoldingId(Long holdingId) { this.holdingId = holdingId; }
    public String getPortfolioId() { return portfolioId; }
    public void setPortfolioId(String portfolioId) { this.portfolioId = portfolioId; }
    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortfolioHolding that = (PortfolioHolding) o;
        return quantity == that.quantity &&
                java.util.Objects.equals(holdingId, that.holdingId) &&
                java.util.Objects.equals(portfolioId, that.portfolioId) &&
                java.util.Objects.equals(instrumentId, that.instrumentId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(holdingId, portfolioId, instrumentId, quantity);
    }

    @Override
    public String toString() {
        return "PortfolioHolding{" +
                "holdingId=" + holdingId +
                ", portfolioId='" + portfolioId + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
