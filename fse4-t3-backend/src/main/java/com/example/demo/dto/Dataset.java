package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dataset {
    private String label;
    private List<? extends Number> data;
    private String borderColor;
    private Boolean fill;
    private Object backgroundColor;

    public Dataset() {}

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<? extends Number> getData() { return data; }
    public void setData(List<? extends Number> data) { this.data = data; }

    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }

    public Boolean getFill() { return fill; }
    public void setFill(Boolean fill) { this.fill = fill; }

    public Object getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Object backgroundColor) { this.backgroundColor = backgroundColor; }
}
