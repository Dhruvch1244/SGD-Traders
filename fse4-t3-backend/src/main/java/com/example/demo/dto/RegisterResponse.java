package com.example.demo.dto;

import com.example.demo.models.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Client client;
    private String fmtsToken;
    private String fmtsClientId;
}
