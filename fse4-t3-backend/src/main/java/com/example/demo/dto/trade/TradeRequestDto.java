package com.example.demo.dto.trade;

// Lombok removed; explicit methods below

public class TradeRequestDto {
    private String clientId;
    private String instrumentId;
    private int quantity;
    private double targetPrice;
    private String direction;
    private String email;
    private String token;
    private String localClientId;
    private String fmtsClientId;

    public TradeRequestDto() {}

    public TradeRequestDto(String clientId, String instrumentId, int quantity, double targetPrice, String direction, String email, String token, String localClientId, String fmtsClientId) {
        this.clientId = clientId;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
        this.targetPrice = targetPrice;
        this.direction = direction;
        this.email = email;
        this.token = token;
        this.localClientId = localClientId;
        this.fmtsClientId = fmtsClientId;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getLocalClientId() { return localClientId; }
    public void setLocalClientId(String localClientId) { this.localClientId = localClientId; }
    public String getFmtsClientId() { return fmtsClientId; }
    public void setFmtsClientId(String fmtsClientId) { this.fmtsClientId = fmtsClientId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeRequestDto that = (TradeRequestDto) o;
        return quantity == that.quantity &&
                Double.compare(that.targetPrice, targetPrice) == 0 &&
                java.util.Objects.equals(clientId, that.clientId) &&
                java.util.Objects.equals(instrumentId, that.instrumentId) &&
                java.util.Objects.equals(direction, that.direction) &&
                java.util.Objects.equals(email, that.email) &&
                java.util.Objects.equals(token, that.token) &&
                java.util.Objects.equals(localClientId, that.localClientId) &&
                java.util.Objects.equals(fmtsClientId, that.fmtsClientId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(clientId, instrumentId, quantity, targetPrice, direction, email, token, localClientId, fmtsClientId);
    }

    @Override
    public String toString() {
        return "TradeRequestDto{" +
                "clientId='" + clientId + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", quantity=" + quantity +
                ", targetPrice=" + targetPrice +
                ", direction='" + direction + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", localClientId='" + localClientId + '\'' +
                ", fmtsClientId='" + fmtsClientId + '\'' +
                '}';
    }
}
