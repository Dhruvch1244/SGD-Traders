package com.example.demo.controller;

import com.example.demo.dto.InstrumentDto;
import com.example.demo.models.InvestmentPreferences;
import com.example.demo.models.Instrument;
import com.example.demo.service.RoboAdvisorService;
import com.example.demo.service.InvestmentPreferencesService;
import com.example.demo.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("api/robo-advisor")
public class RoboAdvisorController {
    @Autowired
    private RoboAdvisorService roboAdvisorService;
    @Autowired
    private InvestmentPreferencesService investmentPreferencesService;
    @Autowired
    private InstrumentService instrumentService;

    @GetMapping("/recommend/{clientId}")
    public ResponseEntity<List<InstrumentDto>> recommend(@PathVariable String clientId) {
        Optional<InvestmentPreferences> preferences = investmentPreferencesService.getPreferences(clientId);
        List<Instrument> availableInstruments = instrumentService.getAllInstruments();
        List<Instrument> recommended = roboAdvisorService.recommendInstrumentsByTier(preferences, availableInstruments);
        List<InstrumentDto> result = recommended.stream()
            .map(InstrumentDto::fromEntity)
            .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
