package com.example.demo.models;

// Lombok removed; explicit methods below
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("RISK_TOLERANCES")
public class RiskTolerance {
    @Id
    @Column("ID")
    private Long id;
    @Column("NAME")
    private String name;

    public RiskTolerance() {}

    public RiskTolerance(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskTolerance that = (RiskTolerance) o;
        return java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "RiskTolerance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
