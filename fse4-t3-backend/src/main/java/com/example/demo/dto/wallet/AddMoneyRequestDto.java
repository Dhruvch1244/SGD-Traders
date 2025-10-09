package com.example.demo.dto.wallet;

import jakarta.validation.constraints.Positive;

public class AddMoneyRequestDto {
    @Positive(message = "Amount must be positive")
    private double amount;

    public AddMoneyRequestDto() {}

    public AddMoneyRequestDto(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddMoneyRequestDto that = (AddMoneyRequestDto) o;
        return Double.compare(that.amount, amount) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amount);
    }

    @Override
    public String toString() {
        return "AddMoneyRequestDto{" +
                "amount=" + amount +
                '}';
    }
}
