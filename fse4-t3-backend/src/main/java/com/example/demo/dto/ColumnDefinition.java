package com.example.demo.dto;


public class ColumnDefinition {
    private String headerName;
    private String field;

    public ColumnDefinition() {}

    public ColumnDefinition(String headerName, String field) {
        this.headerName = headerName;
        this.field = field;
    }

    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
}
