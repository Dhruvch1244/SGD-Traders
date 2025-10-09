package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
// Lombok import removed

// Removed Lombok. Added explicit getters/setters.
public class ForgotPasswordRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotBlank(message = "Date of Birth is required.")
    private String dateOfBirth;

    public ForgotPasswordRequestDto() {}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}
