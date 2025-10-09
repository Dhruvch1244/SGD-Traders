package com.example.demo.dto;

import java.util.List;

public class ChartData {
    private List<String> labels;
    private List<Dataset> datasets;

    public ChartData() {}

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Dataset> getDatasets() { return datasets; }
    public void setDatasets(List<Dataset> datasets) { this.datasets = datasets; }
}
