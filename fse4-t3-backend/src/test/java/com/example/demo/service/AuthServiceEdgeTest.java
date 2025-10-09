package com.example.demo.service;

import com.example.demo.dto.RegistrationRequest;
import com.example.demo.dto.SignInRequest;
import com.example.demo.dto.auth.ChangePasswordRequestDto;
import com.example.demo.dto.auth.ForgotPasswordRequestDto;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.ResourceConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.Client;
import com.example.demo.models.ClientIdentification;
import com.example.demo.repository.ClientIdentificationRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceEdgeTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private ClientIdentificationRepository clientIdentificationRepository;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId("C1");
        client.setEmail("user@example.com");
        client.setPassword("Pass123!");
        client.setDateOfBirth("2000-01-01");
    }

    @Test
    void signIn_invalidPassword_throwsAuthEx() {
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");
        assertThrows(AuthenticationException.class, () -> authService.signIn(req));
    }

    @Test
    void signIn_success_returnsClient() {
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
            when(passwordEncoder.matches("Pass123!", "Pass123!")).thenReturn(true);
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("Pass123!");
        Client result = authService.signIn(req);
        assertNotNull(result);
        assertEquals("C1", result.getClientId());
    }

    @Test
    void register_emailExists_throwsConflict() {
        RegistrationRequest reg = new RegistrationRequest();
        reg.setEmail("user@example.com");
        reg.setPassword("Pass123!");
        reg.setName("User");
        reg.setDateOfBirth("01-01-2000");
        reg.setCountry("IN");
        reg.setPostalCode("600000");
        ClientIdentification cid = new ClientIdentification();
        cid.setId("ID1");
        cid.setType("SSN");
        cid.setValue("123");
        reg.setIdentification(Set.of(cid));
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        assertThrows(ResourceConflictException.class, () -> authService.register(reg));
    }

    @Test
    void register_success_insertsEverything() {
        RegistrationRequest reg = new RegistrationRequest();
        reg.setEmail("new@example.com");
        reg.setPassword("Pass123!");
        reg.setName("User");
        reg.setDateOfBirth("01-01-2000");
        reg.setCountry("IN");
        reg.setPostalCode("600000");
        reg.setIdentification(Set.of());
        when(clientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        // No identifications -> do not stub countByValueIn to avoid unnecessary stubbing error
        when(clientRepository.count()).thenReturn(0L);
        Client created = authService.register(reg);
        assertNotNull(created);
        assertEquals("C1", created.getClientId());
    }

    @Test
    void verifyForgotPassword_dobMismatch_throwsAuthEx() {
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto();
        dto.setEmail("user@example.com");
        dto.setDateOfBirth("2001-01-01");
        assertThrows(AuthenticationException.class, () -> authService.verifyForgotPassword(dto));
    }

    @Test
    void changePassword_emailNotFound_throwsNotFound() {
        when(clientRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto();
        dto.setEmail("missing@example.com");
        dto.setNewPassword("NewPass123!");
        assertThrows(ResourceNotFoundException.class, () -> authService.changePassword(dto));
    }

    @Test
    void changePassword_success_updates() {
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto();
        dto.setEmail("user@example.com");
        dto.setNewPassword("NewPass123!");
        doNothing().when(clientRepository).update(any(Client.class));
        assertDoesNotThrow(() -> authService.changePassword(dto));
        verify(clientRepository).update(any(Client.class));
    }
}


