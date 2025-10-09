package com.example.demo.service;

import com.example.demo.dto.profile.InvestmentPreferencesDataDto;
import com.example.demo.dto.profile.InvestmentPreferencesDto;
import com.example.demo.models.InvestmentPreferences;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InvestmentPreferencesService {

    private final InvestmentPreferencesRepository investmentPreferencesRepository;
    private final RiskToleranceRepository riskToleranceRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final InvestmentPurposeRepository investmentPurposeRepository;
    private final InvestmentDurationRepository investmentDurationRepository;

    public InvestmentPreferencesService(InvestmentPreferencesRepository investmentPreferencesRepository,
                                        RiskToleranceRepository riskToleranceRepository,
                                        IncomeCategoryRepository incomeCategoryRepository,
                                        InvestmentPurposeRepository investmentPurposeRepository,
                                        InvestmentDurationRepository investmentDurationRepository) {
        this.investmentPreferencesRepository = investmentPreferencesRepository;
        this.riskToleranceRepository = riskToleranceRepository;
        this.incomeCategoryRepository = incomeCategoryRepository;
        this.investmentPurposeRepository = investmentPurposeRepository;
        this.investmentDurationRepository = investmentDurationRepository;
    }

    public Optional<InvestmentPreferences> getPreferences(String clientId) {
        return investmentPreferencesRepository.findByClientId(clientId);
    }

    public boolean hasPreferences(String clientId) {
        return investmentPreferencesRepository.findByClientId(clientId).isPresent();
    }

    public static class SaveResult {
        public final InvestmentPreferences preferences;
        public final boolean created;
        public SaveResult(InvestmentPreferences preferences, boolean created) {
            this.preferences = preferences;
            this.created = created;
        }
    }

    @Transactional
    public SaveResult savePreferences(InvestmentPreferencesDto preferencesDto) {
        InvestmentPreferences preferences = mapToEntity(preferencesDto);
        boolean created;
        if (hasPreferences(preferences.getClientId())) {
            investmentPreferencesRepository.update(preferences);
            created = false;
        } else {
            investmentPreferencesRepository.insert(preferences);
            created = true;
        }
        return new SaveResult(preferences, created);
    }

    public InvestmentPreferencesDataDto getInvestmentPreferencesData() {
        InvestmentPreferencesDataDto dataDto = new InvestmentPreferencesDataDto();
        dataDto.setRiskTolerances(riskToleranceRepository.findAll());
        dataDto.setIncomeCategories(incomeCategoryRepository.findAll());
        dataDto.setInvestmentPurposes(investmentPurposeRepository.findAll());
        dataDto.setInvestmentDurations(investmentDurationRepository.findAll());
        return dataDto;


    }

    private InvestmentPreferences mapToEntity(InvestmentPreferencesDto dto) {
        // Null check for clientId
        if (dto.getClientId() == null) {
            throw new NullPointerException("clientId cannot be null");
        }
        // Normalize input to ignore case
        String purposeInput = dto.getInvestmentPurpose() != null ? dto.getInvestmentPurpose().trim().toUpperCase() : null;
        String riskInput = dto.getRiskTolerance() != null ? dto.getRiskTolerance().trim().toUpperCase() : null;
        String incomeInput = dto.getIncomeCategory() != null ? dto.getIncomeCategory().trim().toUpperCase() : null;
        String durationInput = dto.getInvestmentDuration() != null ? dto.getInvestmentDuration().trim().toUpperCase() : null;

        Long purpose = investmentPurposeRepository.findByPurpose(purposeInput);
        Long risk = riskToleranceRepository.findByName(riskInput);
        Long income = incomeCategoryRepository.findByRange(incomeInput);
        Long duration = investmentDurationRepository.findByDuration(durationInput);

        InvestmentPreferences entity = new InvestmentPreferences();
        entity.setClientId(dto.getClientId());
        if (purpose == null) throw new IllegalArgumentException("Unknown investment purpose: " + dto.getInvestmentPurpose());
        entity.setInvestmentPurposeId(purpose);
        if (risk == null) throw new IllegalArgumentException("Unknown risk tolerance: " + dto.getRiskTolerance());
        entity.setRiskToleranceId(risk);
        if (income == null) throw new IllegalArgumentException("Unknown income range: " + dto.getIncomeCategory());
        entity.setIncomeCategoryId(income);
        if (duration == null) throw new IllegalArgumentException("Unknown investment duration: " + dto.getInvestmentDuration());
        entity.setInvestmentDurationId(duration);
        entity.setAcceptedTerms(1L);
        return entity;
    }
}
