package com.example.demo.service;

import com.example.demo.models.InvestmentPreferences;
import com.example.demo.dto.market.MostActiveStockDto;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import com.example.demo.models.Instrument;

@Service
public class RoboAdvisorService {
    // Hardcoded risk mapping for demonstration
    private static final Map<String, String> STOCK_RISK_MAP = new HashMap<>();
    static {
        STOCK_RISK_MAP.put("AAPL", "AGGRESSIVE");
        STOCK_RISK_MAP.put("MSFT", "AVERAGE");
        STOCK_RISK_MAP.put("GOOGL", "ABOVE_AVERAGE");
        STOCK_RISK_MAP.put("JNJ", "CONSERVATIVE");
        STOCK_RISK_MAP.put("PG", "BELOW_AVERAGE");
        STOCK_RISK_MAP.put("TSLA", "AGGRESSIVE");
        STOCK_RISK_MAP.put("AMZN", "ABOVE_AVERAGE");
        STOCK_RISK_MAP.put("KO", "CONSERVATIVE");
        STOCK_RISK_MAP.put("NVDA", "AGGRESSIVE");
        STOCK_RISK_MAP.put("DIS", "AVERAGE");
    }

    // Instrument type ratio mapping by risk tolerance
    private static final Map<String, Map<String, Integer>> RISK_TYPE_RATIO = new HashMap<>();
    static {
        // Ratios for 5 instruments (values must sum to 5)
        RISK_TYPE_RATIO.put("CONSERVATIVE", Map.of(
            "FIXED_INCOME", 3,
            "EQUITY", 1,
            "ETF", 1,
            "COMMODITY", 0,
            "CRYPTO", 0
        ));
        RISK_TYPE_RATIO.put("BELOW_AVERAGE", Map.of(
            "FIXED_INCOME", 2,
            "EQUITY", 2,
            "ETF", 1,
            "COMMODITY", 0,
            "CRYPTO", 0
        ));
        RISK_TYPE_RATIO.put("AVERAGE", Map.of(
            "FIXED_INCOME", 1,
            "EQUITY", 2,
            "ETF", 1,
            "COMMODITY", 1,
            "CRYPTO", 0
        ));
        RISK_TYPE_RATIO.put("ABOVE_AVERAGE", Map.of(
            "FIXED_INCOME", 1,
            "EQUITY", 3,
            "ETF", 0,
            "COMMODITY", 1,
            "CRYPTO", 0
        ));
        RISK_TYPE_RATIO.put("AGGRESSIVE", Map.of(
            "FIXED_INCOME", 0,
            "EQUITY", 3,
            "ETF", 1,
            "COMMODITY", 0,
            "CRYPTO", 1
        ));
    }

    // Tier-based instrument ratios (updated for STOCK, GOVT, CUSIP)
    private static final Map<Integer, Map<String, Integer>> TIER_INSTRUMENT_RATIO = new HashMap<>();
    static {
        TIER_INSTRUMENT_RATIO.put(1, Map.of(
            "GOVT", 3,
            "CUSIP", 1,
            "STOCK", 1
        ));
        TIER_INSTRUMENT_RATIO.put(2, Map.of(
            "GOVT", 1,
            "CUSIP", 2,
            "STOCK", 2
        ));
        TIER_INSTRUMENT_RATIO.put(3, Map.of(
            "GOVT", 0,
            "CUSIP", 1,
            "STOCK", 4
        ));
    }

    public List<MostActiveStockDto> recommendStocks(InvestmentPreferences preferences, List<MostActiveStockDto> availableStocks) {
        String userRisk = getRiskTolerance(preferences.getRiskToleranceId());
        // Filter stocks by risk
        List<MostActiveStockDto> filtered = availableStocks.stream()
            .filter(stock -> STOCK_RISK_MAP.getOrDefault(stock.getInstrumentId(), "AVERAGE").equals(userRisk))
            .collect(Collectors.toList());
        // If not enough, fill with other stocks
        if (filtered.size() < 5) {
            List<MostActiveStockDto> others = availableStocks.stream()
                .filter(stock -> !filtered.contains(stock))
                .limit(5 - filtered.size())
                .collect(Collectors.toList());
            filtered.addAll(others);
        }
        return filtered.stream().limit(5).collect(Collectors.toList());
    }

    public List<Instrument> recommendInstruments(InvestmentPreferences preferences, List<Instrument> availableInstruments) {
        String userRisk = getRiskTolerance(preferences.getRiskToleranceId());
        Map<String, Integer> ratio = RISK_TYPE_RATIO.getOrDefault(userRisk, RISK_TYPE_RATIO.get("AVERAGE"));
        List<Instrument> recommendations = new ArrayList<>();
        Set<String> usedInstrumentIds = new HashSet<>();
        for (Map.Entry<String, Integer> entry : ratio.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            List<Instrument> filtered = availableInstruments.stream()
                .filter(inst -> type.equals(inst.getCategoryId()))
                .filter(inst -> !usedInstrumentIds.contains(inst.getInstrumentId()))
                .limit(count)
                .collect(Collectors.toList());
            recommendations.addAll(filtered);
            filtered.forEach(inst -> usedInstrumentIds.add(inst.getInstrumentId()));
        }
        // If less than 5, fill with any remaining instruments
        if (recommendations.size() < 5) {
            List<Instrument> others = availableInstruments.stream()
                .filter(inst -> !usedInstrumentIds.contains(inst.getInstrumentId()))
                .limit(5 - recommendations.size())
                .collect(Collectors.toList());
            recommendations.addAll(others);
        }
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }

    // Assign user to a risk tier based on all preferences
    public int assignUserTier(Optional<InvestmentPreferences> preferences) {
        InvestmentPreferences prefs = preferences.orElse(null);
        int score = 0;
        // Risk tolerance
        String risk = getRiskTolerance(prefs != null ? prefs.getRiskToleranceId() : null);
        if ("CONSERVATIVE".equals(risk) || "BELOW_AVERAGE".equals(risk)) score += 0;
        else if ("AVERAGE".equals(risk)) score += 1;
        else score += 2;
        // Investment purpose
        String purpose = getPurpose(prefs != null ? prefs.getInvestmentPurposeId() : null);
        if ("RETIREMENT".equals(purpose) || "EDUCATION".equals(purpose)) score += 0;
        else if ("WEALTH_CREATION".equals(purpose)) score += 1;
        // Duration
        String duration = getDuration(prefs != null ? prefs.getInvestmentDurationId() : null);
        if ("ZERO_TO_FIVE_YEARS".equals(duration) || "FIVE_TO_SEVEN_YEARS".equals(duration)) score += 0;
        else score += 1;
        // Income
        int incomeMax = getIncomeMax(prefs != null ? prefs.getIncomeCategoryId() : null);
        if (incomeMax <= 40000) score += 0;
        else if (incomeMax <= 100000) score += 1;
        else score += 2;
        // Tier assignment
        System.out.println("User score for tier assignment: " + score);
        if (score <= 1) return 1; // Low risk
        else if (score <= 3) return 2; // Medium risk
        else return 3; // High risk
    }

    // Recommend instruments based on assigned tier
    public List<Instrument> recommendInstrumentsByTier(Optional<InvestmentPreferences> preferences, List<Instrument> availableInstruments) {
        InvestmentPreferences prefs = preferences.orElse(null);
        int tier = assignUserTier(preferences);
        Map<String, Integer> ratio = TIER_INSTRUMENT_RATIO.getOrDefault(tier, TIER_INSTRUMENT_RATIO.get(2));
        System.out.println("User assigned to tier: " + tier);
        System.out.println("Instrument ratio for tier " + tier + ": " + ratio);
        int userIncomeMax = getIncomeMax(prefs != null ? prefs.getIncomeCategoryId() : null);
        List<Instrument> recommendations = new ArrayList<>();
        Set<String> usedInstrumentIds = new HashSet<>();
        for (Map.Entry<String, Integer> entry : ratio.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            if (count == 0) continue;
            List<Instrument> filtered = availableInstruments.stream()
                .filter(inst -> type.equals(inst.getCategoryId()))
                .filter(inst -> !usedInstrumentIds.contains(inst.getInstrumentId()))
                .filter(inst -> inst.getMinQuantity() <= userIncomeMax)
                .limit(count)
                .collect(Collectors.toList());
            System.out.println("Type: " + type + ", Requested: " + count + ", Found: " + filtered.size());
            recommendations.addAll(filtered);
            filtered.forEach(inst -> usedInstrumentIds.add(inst.getInstrumentId()));
        }
        // Only return what matches the tier ratio, do not fill with other types
        System.out.println("Total recommended instruments: " + recommendations.size());
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }

    private String getRiskTolerance(Long riskToleranceId) {
        // Map riskToleranceId to string (should match your RiskToleranceMap)
        if (riskToleranceId == null) return "AVERAGE";
        switch (riskToleranceId.intValue()) {
            case 1: return "CONSERVATIVE";
            case 2: return "BELOW_AVERAGE";
            case 3: return "AVERAGE";
            case 4: return "ABOVE_AVERAGE";
            case 5: return "AGGRESSIVE";
            default: return "AVERAGE";
        }
    }

    private String getPurpose(Long purposeId) {
        // Map purposeId to string (stub implementation)
        if (purposeId == null) return "WEALTH_CREATION";
        switch (purposeId.intValue()) {
            case 1: return "RETIREMENT";
            case 2: return "EDUCATION";
            case 3: return "WEALTH_CREATION";
            default: return "WEALTH_CREATION";
        }
    }

    private String getDuration(Long durationId) {
        // Map durationId to string (stub implementation)
        if (durationId == null) return "FIVE_TO_SEVEN_YEARS";
        switch (durationId.intValue()) {
            case 1: return "ZERO_TO_FIVE_YEARS";
            case 2: return "FIVE_TO_SEVEN_YEARS";
            case 3: return "SEVEN_TO_TEN_YEARS";
            case 4: return "ABOVE_TEN_YEARS";
            default: return "FIVE_TO_SEVEN_YEARS";
        }
    }

    private int getIncomeMax(Long incomeCategoryId) {
        // Map incomeCategoryId to max income (stub implementation)
        if (incomeCategoryId == null) return 100000;
        switch (incomeCategoryId.intValue()) {
            case 1: return 40000;
            case 2: return 100000;
            case 3: return 200000;
            default: return 100000;
        }
    }
}
