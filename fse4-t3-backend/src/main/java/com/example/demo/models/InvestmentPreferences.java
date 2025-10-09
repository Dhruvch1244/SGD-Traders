package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("INVESTMENT_PREFERENCES")
public class InvestmentPreferences {
    @Id
    @Column("CLIENTID")
    private String clientId;
    @Column("INVESTMENT_PURPOSE_ID")
    private Long investmentPurposeId;
    @Column("RISK_TOLERANCE_ID")
    private Long riskToleranceId;
    @Column("INCOME_CATEGORY_ID")
    private Long incomeCategoryId;
    @Column("INVESTMENT_DURATION_ID")
    private Long investmentDurationId;
    @Column("ACCEPTEDTERMS")
    private Long acceptedTerms;
}
