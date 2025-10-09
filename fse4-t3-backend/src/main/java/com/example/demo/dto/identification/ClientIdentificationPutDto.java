package com.example.demo.dto.identification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientIdentificationPutDto {
    private String type;
    private String value;
}
