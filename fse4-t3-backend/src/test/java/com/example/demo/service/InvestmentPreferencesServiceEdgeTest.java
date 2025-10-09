package com.example.demo.service;

import com.example.demo.dto.profile.InvestmentPreferencesDto;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentPreferencesServiceEdgeTest {

    @Mock InvestmentPreferencesRepository investmentPreferencesRepository;
    @Mock RiskToleranceRepository riskToleranceRepository;
    @Mock IncomeCategoryRepository incomeCategoryRepository;
    @Mock InvestmentPurposeRepository investmentPurposeRepository;
    @Mock InvestmentDurationRepository investmentDurationRepository;

    @InjectMocks InvestmentPreferencesService service;

    @Test
    void mapToEntity_nullClientId_throwsNpe() {
        InvestmentPreferencesDto dto = new InvestmentPreferencesDto();
        assertThrows(NullPointerException.class, () -> service.savePreferences(dto));
    }

    @Test
    void savePreferences_insertsWhenMissing_updatesWhenExists() {
        InvestmentPreferencesDto dto = new InvestmentPreferencesDto();
        dto.setClientId("C1");
        dto.setInvestmentPurpose("Retirement");
        dto.setRiskTolerance("Medium");
        dto.setIncomeCategory("50k-100k");
        dto.setInvestmentDuration("5 years");
        // Required stubs for mapping
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        // Stubs for existence
        when(investmentPreferencesRepository.findByClientId("C1")).thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(new com.example.demo.models.InvestmentPreferences()));

        // first call: insert
        service.savePreferences(dto);
        verify(investmentPreferencesRepository).insert(any(com.example.demo.models.InvestmentPreferences.class));

        // second call: update
        service.savePreferences(dto);
        verify(investmentPreferencesRepository).update(any(com.example.demo.models.InvestmentPreferences.class));
    }
}
