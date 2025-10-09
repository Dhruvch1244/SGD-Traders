package com.example.demo.dto.profile;

public class InvestmentPreferencesDto {
    private String clientId;
    private String investmentPurpose;
    private String riskTolerance;
    private String incomeCategory;
    private String investmentDuration;
    private Long acceptedTerms;

    public InvestmentPreferencesDto() {}

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getInvestmentPurpose() {
        return investmentPurpose;
    }
    public void setInvestmentPurpose(String investmentPurpose) {
        this.investmentPurpose = investmentPurpose;
    }
    public String getRiskTolerance() {
        return riskTolerance;
    }
    public void setRiskTolerance(String riskTolerance) {
        this.riskTolerance = riskTolerance;
    }
    public String getIncomeCategory() {
        return incomeCategory;
    }
    public void setIncomeCategory(String incomeCategory) {
        this.incomeCategory = incomeCategory;
    }
    public String getInvestmentDuration() {
        return investmentDuration;
    }
    public void setInvestmentDuration(String investmentDuration) {
        this.investmentDuration = investmentDuration;
    }
    public Long getAcceptedTerms() {
        return acceptedTerms;
    }
    public void setAcceptedTerms(Long acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }
}
