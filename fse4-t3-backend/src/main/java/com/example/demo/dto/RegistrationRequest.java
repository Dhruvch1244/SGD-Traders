package com.example.demo.dto;

import com.example.demo.models.ClientIdentification;
import com.example.demo.validation.Adult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
// Lombok import removed

import java.util.Set;

// Removed Lombok. Added explicit getters/setters.
public class RegistrationRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @Pattern(regexp = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d\\d$", message = "Date of Birth must be in MM-DD-YYYY format.")
    @Adult
    private String dateOfBirth;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "Postal Code is required.")
    private String postalCode;

    @Valid
    @NotEmpty(message = "At least one identification document is required.")
    private Set<ClientIdentification> identification;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$", message = "PasswordCriteria of 8 char, 1 num, 1 spl char not met")
    private String password;

    public RegistrationRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public Set<ClientIdentification> getIdentification() { return identification; }
    public void setIdentification(Set<ClientIdentification> identification) { this.identification = identification; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
