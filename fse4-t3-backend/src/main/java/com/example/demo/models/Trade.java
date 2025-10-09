package com.example.demo.models;

// Lombok removed; explicit methods below
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("TRADES")
public class Trade implements Persistable<String> {
    @Id
    @Column("TRADEID")
    private String tradeId;
    @Column("INSTRUMENTID")
    private String instrumentId;
    @Column("QUANTITY")
    private int quantity;
    @Column("EXECUTIONPRICE")
    private double executionPrice;
    @Column("DIRECTION")
    private String direction;
    @Column("CLIENTID")
    private String clientId;
    @Column("ORDERID")
    private String orderId;
    @Column("CASHVALUE")
    private double cashValue;
    @Column("TIMESTAMP")
    private LocalDateTime timestamp;

    @Transient
    private boolean isNew = true;

    private Order order; // Add order object reference

    public Trade() {}

    public Trade(String tradeId, String instrumentId, int quantity, double executionPrice, String direction, String clientId, String orderId, double cashValue, LocalDateTime timestamp) {
        this.tradeId = tradeId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.direction = direction;
        this.clientId = clientId;
        this.orderId = orderId;
        this.cashValue = cashValue;
        this.timestamp = timestamp;
    }

    // Added constructor to match test usage (with isNew)
    public Trade(String tradeId, String instrumentId, int quantity, double executionPrice, String direction, String clientId, String orderId, double cashValue, LocalDateTime timestamp, boolean isNew) {
        this.tradeId = tradeId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.executionPrice = executionPrice;
        this.direction = direction;
        this.clientId = clientId;
        this.orderId = orderId;
        this.cashValue = cashValue;
        this.timestamp = timestamp;
        this.isNew = isNew;
    }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getExecutionPrice() { return executionPrice; }
    public void setExecutionPrice(double executionPrice) { this.executionPrice = executionPrice; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public double getCashValue() { return cashValue; }
    public void setCashValue(double cashValue) { this.cashValue = cashValue; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    @Override
    public String getId() {
        return this.tradeId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public void setAsNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return quantity == trade.quantity &&
                Double.compare(trade.executionPrice, executionPrice) == 0 &&
                Double.compare(trade.cashValue, cashValue) == 0 &&
                java.util.Objects.equals(tradeId, trade.tradeId) &&
                java.util.Objects.equals(instrumentId, trade.instrumentId) &&
                java.util.Objects.equals(direction, trade.direction) &&
                java.util.Objects.equals(clientId, trade.clientId) &&
                java.util.Objects.equals(orderId, trade.orderId) &&
                java.util.Objects.equals(timestamp, trade.timestamp);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(tradeId, instrumentId, quantity, executionPrice, direction, clientId, orderId, cashValue, timestamp);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", quantity=" + quantity +
                ", executionPrice=" + executionPrice +
                ", direction='" + direction + '\'' +
                ", clientId='" + clientId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", cashValue=" + cashValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
