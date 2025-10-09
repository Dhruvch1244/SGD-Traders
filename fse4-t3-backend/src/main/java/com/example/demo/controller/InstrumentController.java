package com.example.demo.controller;

import com.example.demo.models.Instrument;
import com.example.demo.service.InstrumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    @GetMapping
    public List<Instrument> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instrument> getInstrumentById(@PathVariable String id) {
        Optional<Instrument> instrument = instrumentService.getInstrumentById(id);
        return instrument.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
