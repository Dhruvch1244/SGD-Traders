package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.RegistrationRequest;
import com.example.demo.dto.SignInRequest;
import com.example.demo.dto.SignInResponse;
import com.example.demo.dto.auth.ChangePasswordRequestDto;
import com.example.demo.dto.auth.ForgotPasswordRequestDto;
import com.example.demo.dto.RegisterResponse;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.AuthService;
import com.example.demo.service.InvestmentPreferencesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final InvestmentPreferencesService preferencesService;

    public AuthController(AuthService authService, InvestmentPreferencesService preferencesService) {
        this.authService = authService;
        this.preferencesService = preferencesService;
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> signIn(@Valid @RequestBody SignInRequest signInRequest) {
        SignInResponse signInResponse = authService.signInWithFmts(signInRequest);
        boolean hasPreferences = preferencesService.hasPreferences(signInResponse.getClient().getClientId());
        String message = hasPreferences ? "Sign-in successful" : "Investment preferences not set";
        return ResponseEntity.ok(new ApiResponse(hasPreferences, message, signInResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        RegisterResponse registerResponse = authService.registerWithFmts(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Client registration successful", registerResponse));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.verifyForgotPassword(request);
        return ResponseEntity.ok(new ApiResponse(true, "Verification successful. You can now change your password."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        authService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully."));
    }

    // -- Exception Handlers --

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse> handleInvalidRequestException(InvalidRequestException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, ex.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse> handleResourceConflictException(ResourceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "An unexpected error occurred: " + ex.getMessage()));
    }
}
