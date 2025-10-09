package com.example.demo.models;

// Lombok removed; explicit methods below
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("WALLETS")
public class Wallet {
    @Id
    @Column("CLIENTID")
    private String clientId;
    @Column("BALANCE")
    private double balance;

    public Wallet() {}

    public Wallet(String clientId, double balance) {
        this.clientId = clientId;
        this.balance = balance;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Double.compare(wallet.balance, balance) == 0 &&
                java.util.Objects.equals(clientId, wallet.clientId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(clientId, balance);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "clientId='" + clientId + '\'' +
                ", balance=" + balance +
                '}';
    }
}
