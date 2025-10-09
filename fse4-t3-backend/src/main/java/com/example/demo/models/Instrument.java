package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public class Instrument {
    @Id
    @Column("INSTRUMENTID")
    private String instrumentId;
    @Column("DESCRIPTION")
    private String description;
    @Column("EXTERNALIDTYPE")
    private String externalIdType;
    @Column("EXTERNALID")
    private String externalId;
    @Column("CATEGORYID")
    private String categoryId;
    @Column("MINQUANTITY")
    private int minQuantity;
    @Column("MAXQUANTITY")
    private int maxQuantity;

    public Instrument() {}

    public Instrument(String instrumentId, String description, String externalIdType, String externalId, String categoryId, int minQuantity, int maxQuantity) {
        this.instrumentId = instrumentId;
        this.description = description;
        this.externalIdType = externalIdType;
        this.externalId = externalId;
        this.categoryId = categoryId;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public String getInstrumentId() { return instrumentId; }
    public void setInstrumentId(String instrumentId) { this.instrumentId = instrumentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExternalIdType() { return externalIdType; }
    public void setExternalIdType(String externalIdType) { this.externalIdType = externalIdType; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public int getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(int maxQuantity) { this.maxQuantity = maxQuantity; }
}
