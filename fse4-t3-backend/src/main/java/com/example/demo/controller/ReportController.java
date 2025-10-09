package com.example.demo.controller;

import com.example.demo.dto.report.ReportDto;
import com.example.demo.models.report.TimeScale;
import com.example.demo.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ReportDto> generateReport(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "ALL_TIME") TimeScale timeScale) {
        try {
            ReportDto report = reportService.generateReport(clientId, timeScale);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            // This will catch cases where the client or wallet is not found
            return ResponseEntity.notFound().build();
        }
    }
}
