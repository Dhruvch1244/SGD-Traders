package com.example.demo.dto.trade;

import com.example.demo.models.Trade;
// Lombok fully removed; explicit methods implemented below
public class TradeResponseDto {
    public TradeResponseDto(boolean success, String message, Trade trade) {
        this.success = success;
        this.message = message;
        this.trade = trade;
    }
    private boolean success;
    private String message;
    private Trade trade;

    public TradeResponseDto() {}

    public TradeResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeResponseDto that = (TradeResponseDto) o;
        return success == that.success &&
                java.util.Objects.equals(message, that.message) &&
                java.util.Objects.equals(trade, that.trade);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(success, message, trade);
    }

    @Override
    public String toString() {
        return "TradeResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", trade=" + trade +
                '}';
    }
}
