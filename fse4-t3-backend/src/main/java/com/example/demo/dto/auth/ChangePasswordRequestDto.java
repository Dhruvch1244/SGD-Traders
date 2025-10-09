package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
// Lombok import removed

// Removed Lombok. Added explicit getters/setters.
public class ChangePasswordRequestDto {
    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$", message = "Password must be at least 8 characters long and contain at least one number, one letter, and one special character.")
    private String newPassword;

    public ChangePasswordRequestDto() {}
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
