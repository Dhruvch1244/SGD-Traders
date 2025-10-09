package com.example.demo.dto.profile;

import com.example.demo.models.ClientIdentification;
import lombok.Data;

import java.util.Set;

@Data
public class ProfileDto {
    private String clientId;
    private String name;
    private String email;
    private String dateOfBirth;
    private String country;
    private String postalCode;
    private Set<ClientIdentification> identification;
}
