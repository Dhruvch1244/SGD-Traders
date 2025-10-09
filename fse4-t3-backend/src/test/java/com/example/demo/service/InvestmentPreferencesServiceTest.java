package com.example.demo.service;

import com.example.demo.dto.profile.InvestmentPreferencesDataDto;
import com.example.demo.dto.profile.InvestmentPreferencesDto;
import com.example.demo.models.InvestmentPreferences;
import com.example.demo.models.RiskTolerance;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentPreferencesServiceTest {
    @Mock InvestmentPreferencesRepository investmentPreferencesRepository;
    @Mock RiskToleranceRepository riskToleranceRepository;
    @Mock IncomeCategoryRepository incomeCategoryRepository;
    @Mock InvestmentPurposeRepository investmentPurposeRepository;
    @Mock InvestmentDurationRepository investmentDurationRepository;

    @InjectMocks InvestmentPreferencesService service;

    InvestmentPreferencesDto dto;
    InvestmentPreferences prefs;

    @BeforeEach
    void setup() {
        dto = new InvestmentPreferencesDto();
        dto.setClientId("C1");
        dto.setInvestmentPurpose("Retirement");
        dto.setRiskTolerance("Medium");
        dto.setIncomeCategory("50k-100k");
        dto.setInvestmentDuration("5 years");
        prefs = new InvestmentPreferences();
        prefs.setClientId("C1");
        prefs.setInvestmentPurposeId(1L);
        prefs.setRiskToleranceId(2L);
        prefs.setIncomeCategoryId(3L);
        prefs.setInvestmentDurationId(4L);
        prefs.setAcceptedTerms(1L);
    }

    @Test
    void getPreferences_found() {
        when(investmentPreferencesRepository.findByClientId("C1")).thenReturn(Optional.of(prefs));
        Optional<InvestmentPreferences> result = service.getPreferences("C1");
        assertTrue(result.isPresent());
        assertEquals(prefs, result.get());
    }

    @Test
    void getPreferences_notFound() {
        when(investmentPreferencesRepository.findByClientId("C2")).thenReturn(Optional.empty());
        Optional<InvestmentPreferences> result = service.getPreferences("C2");
        assertFalse(result.isPresent());
    }

    @Test
    void hasPreferences_true() {
        when(investmentPreferencesRepository.findByClientId("C1")).thenReturn(Optional.of(prefs));
        assertTrue(service.hasPreferences("C1"));
    }

    @Test
    void hasPreferences_false() {
        when(investmentPreferencesRepository.findByClientId("C2")).thenReturn(Optional.empty());
        assertFalse(service.hasPreferences("C2"));
    }

    @Test
    void savePreferences_insertsIfNotExists() {
        when(investmentPreferencesRepository.findByClientId("C1")).thenReturn(Optional.empty());
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        doNothing().when(investmentPreferencesRepository).insert(any());
    InvestmentPreferences result = service.savePreferences(dto).preferences;
        assertEquals("C1", result.getClientId());
        verify(investmentPreferencesRepository).insert(any());
    }

    @Test
    void savePreferences_updatesIfExists() {
        when(investmentPreferencesRepository.findByClientId("C1")).thenReturn(Optional.of(prefs));
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        doNothing().when(investmentPreferencesRepository).update(any());
    InvestmentPreferences result = service.savePreferences(dto).preferences;
        assertEquals("C1", result.getClientId());
        verify(investmentPreferencesRepository).update(any());
    }

    @Test
    void savePreferences_throwsForUnknownPurpose() {
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(null);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        assertThrows(IllegalArgumentException.class, () -> service.savePreferences(dto));
    }

    @Test
    void savePreferences_throwsForUnknownRisk() {
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(null);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        assertThrows(IllegalArgumentException.class, () -> service.savePreferences(dto));
    }

    @Test
    void savePreferences_throwsForUnknownIncome() {
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(null);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(4L);
        assertThrows(IllegalArgumentException.class, () -> service.savePreferences(dto));
    }

    @Test
    void savePreferences_throwsForUnknownDuration() {
    when(investmentPurposeRepository.findByPurpose("RETIREMENT")).thenReturn(1L);
    when(riskToleranceRepository.findByName("MEDIUM")).thenReturn(2L);
    when(incomeCategoryRepository.findByRange("50K-100K")).thenReturn(3L);
    when(investmentDurationRepository.findByDuration("5 YEARS")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.savePreferences(dto));
    }

    @Test
    void getInvestmentPreferencesData_returnsAllLists() {
        when(riskToleranceRepository.findAll()).thenReturn(Arrays.asList(new RiskTolerance(), new RiskTolerance()));
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(investmentPurposeRepository.findAll()).thenReturn(Collections.emptyList());
        when(investmentDurationRepository.findAll()).thenReturn(Collections.emptyList());
        InvestmentPreferencesDataDto result = service.getInvestmentPreferencesData();
        assertEquals(2, result.getRiskTolerances().size());
        assertTrue(result.getIncomeCategories().isEmpty());
        assertTrue(result.getInvestmentPurposes().isEmpty());
        assertTrue(result.getInvestmentDurations().isEmpty());
    }
}

