package com.example.demo.service;

import com.example.demo.dto.InstrumentDto;
import com.example.demo.models.Instrument;
import com.example.demo.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    public Optional<Instrument> getInstrumentById(String id) {
        return instrumentRepository.findById(id);
    }

    public List<InstrumentDto> searchInstruments(String description, String category) {
        List<Instrument> instruments = instrumentRepository.findByCriteria(description, category);
    return instruments.stream()
        .map(instrument -> new InstrumentDto(instrument.getInstrumentId(), instrument.getDescription(), instrument.getCategoryId()))
        .collect(Collectors.toList());
    }
}
