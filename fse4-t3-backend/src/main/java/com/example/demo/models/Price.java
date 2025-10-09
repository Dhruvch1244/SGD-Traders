package com.example.demo.models;

// Lombok removed; explicit methods below
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Table("PRICES")
public class Price implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column("INSTRUMENTID")
    private String instrumentId;
    @Column("BIDPRICE")
    private double bidPrice;
    @Column("ASKPRICE")
    private double askPrice;
    @Column("TIMESTAMP")
    private LocalDateTime timestamp;

    public Price() {}

    public Price(String instrumentId, double bidPrice, double askPrice, LocalDateTime timestamp) {
        this.instrumentId = instrumentId;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.timestamp = timestamp;
    }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public double getBidPrice() { return bidPrice; }
    public void setBidPrice(double bidPrice) { this.bidPrice = bidPrice; }
    public double getAskPrice() { return askPrice; }
    public void setAskPrice(double askPrice) { this.askPrice = askPrice; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return Double.compare(price.bidPrice, bidPrice) == 0 &&
                Double.compare(price.askPrice, askPrice) == 0 &&
                java.util.Objects.equals(instrumentId, price.instrumentId) &&
                java.util.Objects.equals(timestamp, price.timestamp);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(instrumentId, bidPrice, askPrice, timestamp);
    }

    @Override
    public String toString() {
        return "Price{" +
                "instrumentId='" + instrumentId + '\'' +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", timestamp=" + timestamp +
                '}';
    }
}
