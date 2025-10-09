package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDto {
    private String instrumentId;
    private String description;
    private String category;

    public static InstrumentDto fromEntity(com.example.demo.models.Instrument instrument) {
        if (instrument == null) return null;
        return new InstrumentDto(
            instrument.getInstrumentId(),
            instrument.getDescription(),
            instrument.getCategoryId()
        );
    }
}
