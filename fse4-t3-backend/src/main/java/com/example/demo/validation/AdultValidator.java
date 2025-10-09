package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AdultValidator implements ConstraintValidator<Adult, String> {

    @Override
    public boolean isValid(String dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            return true; // Let @NotBlank handle this
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
            LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
            LocalDate today = LocalDate.now();

            // Check if the birth date is in the future
            if (birthDate.isAfter(today)) {
                return false;
            }

            // Check if the user is at least 18 years old
            return !birthDate.plusYears(18).isAfter(today);

        } catch (DateTimeParseException e) {
            // The @Pattern annotation in the DTO should catch format errors,
            // but this provides an extra layer of safety.
            return false;
        }
    }
}
