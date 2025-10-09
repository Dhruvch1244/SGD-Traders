package com.example.demo.models;


public class Order {
    private String orderId;
    private String instrumentId;
    private int quantity;
    private double targetPrice;
    private String direction;
    private String clientId;

    public Order() {}

    public Order(String orderId, String instrumentId, int quantity, double targetPrice, String direction, String clientId) {
        this.orderId = orderId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.targetPrice = targetPrice;
        this.direction = direction;
        this.clientId = clientId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return quantity == order.quantity &&
                Double.compare(order.targetPrice, targetPrice) == 0 &&
                java.util.Objects.equals(orderId, order.orderId) &&
                java.util.Objects.equals(instrumentId, order.instrumentId) &&
                java.util.Objects.equals(direction, order.direction) &&
                java.util.Objects.equals(clientId, order.clientId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(orderId, instrumentId, quantity, targetPrice, direction, clientId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", quantity=" + quantity +
                ", targetPrice=" + targetPrice +
                ", direction='" + direction + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
