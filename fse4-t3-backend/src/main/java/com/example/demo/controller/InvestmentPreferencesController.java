package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.profile.InvestmentPreferencesDataDto;
import com.example.demo.dto.profile.InvestmentPreferencesDto;
import com.example.demo.models.InvestmentPreferences;
import com.example.demo.service.InvestmentPreferencesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;
@RestController
@RequestMapping("/api/preferences")
public class InvestmentPreferencesController {

    private final InvestmentPreferencesService preferencesService;

    public InvestmentPreferencesController(InvestmentPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<InvestmentPreferences> getPreferences(@PathVariable String clientId) {
        Optional<InvestmentPreferences> preferences = preferencesService.getPreferences(clientId);
        return preferences.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/data")
    public ResponseEntity<InvestmentPreferencesDataDto> getPreferencesData() {
        return ResponseEntity.ok(preferencesService.getInvestmentPreferencesData());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> savePreferences(@RequestBody InvestmentPreferencesDto preferencesDto) {
        try {
            InvestmentPreferencesService.SaveResult result = preferencesService.savePreferences(preferencesDto);
            String message = result.created ? "Preferences created successfully" : "Preferences updated successfully";
            return ResponseEntity.ok(new ApiResponse(true, message, result.preferences));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to save preferences: " + e.getMessage()));
        }
    }
}
