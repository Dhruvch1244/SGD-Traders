package com.example.demo.dto.profile;

import com.example.demo.models.IncomeCategory;
import com.example.demo.models.InvestmentDuration;
import com.example.demo.models.InvestmentPurpose;
import com.example.demo.models.RiskTolerance;
import lombok.Data;

import java.util.List;

@Data
public class InvestmentPreferencesDataDto {
    private List<RiskTolerance> riskTolerances;
    private List<IncomeCategory> incomeCategories;
    private List<InvestmentPurpose> investmentPurposes;
    private List<InvestmentDuration> investmentDurations;
}
