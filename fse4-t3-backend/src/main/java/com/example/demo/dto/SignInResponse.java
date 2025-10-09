package com.example.demo.dto;

import com.example.demo.models.Client;
// Lombok imports removed

// Removed Lombok. Added explicit getters/setters.
public class SignInResponse {
    private Client client;
    private String fmtsToken;
    private String fmtsClientId;

    public SignInResponse() {}
    public SignInResponse(Client client, String fmtsToken, String fmtsClientId) {
        this.client = client;
        this.fmtsToken = fmtsToken;
        this.fmtsClientId = fmtsClientId;
    }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getFmtsToken() { return fmtsToken; }
    public void setFmtsToken(String fmtsToken) { this.fmtsToken = fmtsToken; }
    public String getFmtsClientId() { return fmtsClientId; }
    public void setFmtsClientId(String fmtsClientId) { this.fmtsClientId = fmtsClientId; }
}