package com.example.demo.service;

import com.example.demo.dto.RegistrationRequest;
import com.example.demo.dto.SignInRequest;
import com.example.demo.dto.auth.ChangePasswordRequestDto;
import com.example.demo.dto.auth.ForgotPasswordRequestDto;
import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.Client;
import com.example.demo.models.ClientIdentification;
import com.example.demo.models.Wallet;
import com.example.demo.repository.ClientIdentificationRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
    @Test
    void getFmtsResponse_nonJson_returnsNullFields() {
        Client client = new Client();
        client.setEmail("user@example.com");
        String fmtsHtml = "<html>not json</html>";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsHtml);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate, passwordEncoder);
        var result = service.getFmtsResponse(client);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
    }

    @Test
    void normalizeDate_handlesNullBlankInvalidAndValid() throws Exception {
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate, passwordEncoder);
        // null input
        assertNull(invokeNormalizeDate(service, null));
        // blank input
        assertEquals("", invokeNormalizeDate(service, ""));
        // invalid input
        assertEquals("notadate", invokeNormalizeDate(service, "notadate"));
        // valid yyyy-MM-dd
        assertEquals("2020-01-02", invokeNormalizeDate(service, "2020-01-02"));
        // valid MM-dd-yyyy (should normalize to 2020-02-01)
        assertEquals("2020-02-01", invokeNormalizeDate(service, "02-01-2020"));
        // valid MM/dd/yyyy (should normalize to 2020-01-02)
        assertEquals("2020-01-02", invokeNormalizeDate(service, "01/02/2020"));
    }

    // Helper to invoke private normalizeDate
    private String invokeNormalizeDate(AuthService service, String input) throws Exception {
        java.lang.reflect.Method m = AuthService.class.getDeclaredMethod("normalizeDate", String.class);
        m.setAccessible(true);
        return (String) m.invoke(service, input);
    }

    @Test
    void safeDatesEqual_variousCases() throws Exception {
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate, passwordEncoder);
        java.lang.reflect.Method m = AuthService.class.getDeclaredMethod("safeDatesEqual", String.class, String.class);
        m.setAccessible(true);
        // both null
        assertTrue((Boolean) m.invoke(service, null, null));
        // one null
        assertFalse((Boolean) m.invoke(service, null, "2020-01-01"));
        assertFalse((Boolean) m.invoke(service, "2020-01-01", null));
        // both non-null, not equal after normalization
        assertFalse((Boolean) m.invoke(service, "2020-01-01", "2021-01-01"));
        // both non-null, equal after normalization
        assertTrue((Boolean) m.invoke(service, "2020-01-01", "2020-01-01"));
        // both non-null, equal after normalization with different formats
        assertTrue((Boolean) m.invoke(service, "01-01-2020", "2020-01-01"));
    }
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

    @BeforeEach
    void setUp() {
        AutoCloseable mocks = MockitoAnnotations.openMocks(this);
        // Default: passwordEncoder.matches returns true if raw and encoded are equal
        when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> {
            String raw = invocation.getArgument(0);
            String encoded = invocation.getArgument(1);
            return raw.equals(encoded);
        });
        // Default: passwordEncoder.encode returns the raw password
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Service instantiates and dependencies are injected")
    void testAuthServiceInstantiation() {
        assertNotNull(authService);
    }

    @Test
    @DisplayName("Registration fails when email already exists")
    void testRegisterFailsWhenEmailExists() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setName("Test");
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new Client()));
        Exception ex = assertThrows(ResourceConflictException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    @DisplayName("Registration fails when identification already exists")
    void testRegisterFailsWhenIdentificationExists() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("unique@example.com");
        req.setName("Test");
        req.setPassword("password");
        ClientIdentification id = new ClientIdentification();
        id.setValue("ID123");
        req.setIdentification(Set.of(id));
        when(clientRepository.findByEmail("unique@example.com")).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(1L);
        Exception ex = assertThrows(ResourceConflictException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Client details already exist"));
    }

    @Test
    @DisplayName("Sign in fails with wrong password")
    void testSignInFailsWithWrongPassword() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrongpass");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("correctpass");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        // Simulate password mismatch
        when(passwordEncoder.matches("wrongpass", "correctpass")).thenReturn(false);
        assertThrows(AuthenticationException.class, () -> authService.signIn(req));
    }

    @Test
    @DisplayName("Register with minimal valid data")
    void testRegisterWithMinimalValidData() {
        RegistrationRequest req = new RegistrationRequest();
        req.setName("A");
        req.setEmail("min@example.com");
        req.setDateOfBirth("01-01-2000");
        req.setCountry("US");
        req.setPostalCode("1");
        req.setPassword("p");
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        when(clientRepository.count()).thenReturn(0L);
        doNothing().when(clientRepository).insert(any(Client.class));
        doNothing().when(walletRepository).insert(any(Wallet.class));
        Client result = authService.register(req);
        assertNotNull(result);
        assertEquals("min@example.com", result.getEmail());
        assertEquals("A", result.getName());
        assertEquals("2000-01-01", result.getDateOfBirth());
    }

    @Test
    @DisplayName("Change password happy path")
    void testChangePasswordHappyPath() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail("user@example.com");
        req.setNewPassword("newpass");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("oldpass");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        doAnswer(invocation -> {
            Client updated = invocation.getArgument(0);
            client.setPassword(updated.getPassword());
            return null;
        }).when(clientRepository).update(any(Client.class));
        when(passwordEncoder.encode("newpass")).thenReturn("newpass");
        assertDoesNotThrow(() -> authService.changePassword(req));
        assertEquals("newpass", client.getPassword());
    }

    @Test
    @DisplayName("Forgot password verification fails with wrong DOB")
    void testForgotPasswordVerificationFailsWithWrongDob() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth("01-01-1900");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setDateOfBirth("2000-01-01");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        assertThrows(AuthenticationException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("Forgot password verification succeeds with correct DOB")
    void testForgotPasswordVerificationSucceedsWithCorrectDob() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth("01-01-2000");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setDateOfBirth("2000-01-01");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        assertDoesNotThrow(() -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("signIn throws for null request")
    void testSignInNullRequest() {
        assertThrows(InvalidRequestException.class, () -> authService.signIn(null));
    }

    @Test
    @DisplayName("signIn throws for email not found")
    void testSignInEmailNotFound() {
        SignInRequest req = new SignInRequest();
        req.setEmail("notfound@example.com");
        req.setPassword("pass");
        when(clientRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.signIn(req));
    }

    @Test
    @DisplayName("signIn returns client for correct password")
    void testSignInCorrectPassword() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("correctpass");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("correctpass");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        Client result = authService.signIn(req);
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    @DisplayName("register throws for null request")
    void testRegisterNullRequest() {
        assertThrows(InvalidRequestException.class, () -> authService.register(null));
    }

    @Test
    @DisplayName("register throws for null identification set")
    void testRegisterNullIdentification() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setName("name");
        req.setPassword("password");
        req.setIdentification(null);
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("register throws for null password")
    void testRegisterNullPassword() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setName("name");
        req.setPassword(null);
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("register throws for empty password")
    void testRegisterEmptyPassword() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setName("name");
        req.setPassword("");
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("register throws for null email")
    void testRegisterNullEmail() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail(null);
        req.setName("name");
        req.setPassword("pass");
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("register throws for empty email")
    void testRegisterEmptyEmail() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("");
        req.setName("name");
        req.setPassword("pass");
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("register throws for null name")
    void testRegisterNullName() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");
        req.setName(null);
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("verifyForgotPassword throws for null request")
    void testVerifyForgotPasswordNullRequest() {
        assertThrows(InvalidRequestException.class, () -> authService.verifyForgotPassword(null));
    }

    @Test
    @DisplayName("verifyForgotPassword throws for email not found")
    void testVerifyForgotPasswordEmailNotFound() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("notfound@example.com");
        req.setDateOfBirth("2000-01-01");
        when(clientRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("verifyForgotPassword throws for null DOB")
    void testVerifyForgotPasswordNullDob() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth(null);
        assertThrows(InvalidRequestException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("changePassword throws for null request")
    void testChangePasswordNullRequest() {
        assertThrows(InvalidRequestException.class, () -> authService.changePassword(null));
    }

    @Test
    @DisplayName("changePassword throws for email not found")
    void testChangePasswordEmailNotFound() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail("notfound@example.com");
        req.setNewPassword("newpass");
        when(clientRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.changePassword(req));
    }

    @Test
    @DisplayName("changePassword throws for null new password")
    void testChangePasswordNullNewPassword() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail("user@example.com");
        req.setNewPassword(null);
        assertThrows(InvalidRequestException.class, () -> authService.changePassword(req));
    }

    // --- Edge Case Tests ---

    @Test
    @DisplayName("signIn throws for blank email")
    void testSignInWithBlankEmail() {
        SignInRequest req = new SignInRequest();
        req.setEmail(" ");
        req.setPassword("password");
        assertThrows(InvalidRequestException.class, () -> authService.signIn(req));
    }

    @Test
    @DisplayName("signIn throws for blank password")
    void testSignInWithBlankPassword() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword(" ");
        assertThrows(InvalidRequestException.class, () -> authService.signIn(req));
    }

    @Test
    @DisplayName("signIn is case-insensitive to email")
    void testSignInCaseInsensitiveEmail() {
        SignInRequest req = new SignInRequest();
        req.setEmail("USER@EXAMPLE.COM");
        req.setPassword("correctpass");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("correctpass");
        when(clientRepository.findByEmail("USER@EXAMPLE.COM")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        Client result = authService.signIn(req);
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    @DisplayName("register throws for blank name")
    void testRegisterWithBlankName() {
        RegistrationRequest req = new RegistrationRequest();
        req.setName(" ");
        req.setEmail("test@example.com");
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        assertThrows(InvalidRequestException.class, () -> authService.register(req));
    }

    @ParameterizedTest
    @ValueSource(strings = {"01/01/2000", "2000-01-01"})
    @DisplayName("register handles various valid date formats")
    void testRegisterWithVariousDateFormats(String dob) {
        RegistrationRequest req = new RegistrationRequest();
        req.setName("Test");
        req.setEmail("test@example.com");
        req.setPassword("password");
        req.setDateOfBirth(dob);
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        when(clientRepository.count()).thenReturn(0L);
        Client result = authService.register(req);
        assertEquals("2000-01-01", result.getDateOfBirth());
    }

    @Test
    @DisplayName("register handles identification with null value")
    void testRegisterWithNullIdentificationValue() {
        RegistrationRequest req = new RegistrationRequest();
        req.setName("Test");
        req.setEmail("test@example.com");
        req.setPassword("password");
        ClientIdentification id = new ClientIdentification();
        id.setValue(null);
        req.setIdentification(Set.of(id));
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        assertDoesNotThrow(() -> authService.register(req));
    }

    @Test
    @DisplayName("register handles identification with empty value")
    void testRegisterWithEmptyIdentificationValue() {
        RegistrationRequest req = new RegistrationRequest();
        req.setName("Test");
        req.setEmail("test@example.com");
        req.setPassword("password");
        ClientIdentification id = new ClientIdentification();
        id.setValue("");
        req.setIdentification(Set.of(id));
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        assertDoesNotThrow(() -> authService.register(req));
    }

    @Test
    @DisplayName("register handles identification set with null object")
    void testRegisterWithNullIdentificationObject() {
        RegistrationRequest req = new RegistrationRequest();
        req.setName("Test");
        req.setEmail("test@example.com");
        req.setPassword("password");
        req.setIdentification(Collections.singleton(null));
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(NullPointerException.class, () -> authService.register(req));
    }

    @ParameterizedTest
    @ValueSource(strings = {"01/01/2000", "2000-01-01"})
    @DisplayName("verifyForgotPassword handles various valid date formats")
    void testVerifyForgotPasswordWithVariousDateFormats(String dob) {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth(dob);
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setDateOfBirth("2000-01-01");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        assertDoesNotThrow(() -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("verifyForgotPassword throws for blank email")
    void testVerifyForgotPasswordWithBlankEmail() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail(" ");
        req.setDateOfBirth("2000-01-01");
        assertThrows(InvalidRequestException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("verifyForgotPassword throws for blank DOB")
    void testVerifyForgotPasswordWithBlankDob() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth(" ");
        assertThrows(InvalidRequestException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("changePassword throws for blank email")
    void testChangePasswordWithBlankEmail() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail(" ");
        req.setNewPassword("newpass");
        assertThrows(InvalidRequestException.class, () -> authService.changePassword(req));
    }

    @Test
    @DisplayName("changePassword throws for blank new password")
    void testChangePasswordWithBlankNewPassword() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail("user@example.com");
        req.setNewPassword(" ");
        assertThrows(InvalidRequestException.class, () -> authService.changePassword(req));
    }

    @Test
    @DisplayName("register throws when clientRepository throws exception")
    void testRegisterRepositoryThrows() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("test@example.com");
        req.setName("Test");
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("signIn throws when clientRepository throws exception")
    void testSignInRepositoryThrows() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        when(clientRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> authService.signIn(req));
    }

    @Test
    @DisplayName("changePassword throws when clientRepository throws exception")
    void testChangePasswordRepositoryThrows() {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setEmail("user@example.com");
        req.setNewPassword("newpass");
        when(clientRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> authService.changePassword(req));
    }

    @Test
    @DisplayName("verifyForgotPassword throws when clientRepository throws exception")
    void testVerifyForgotPasswordRepositoryThrows() {
        ForgotPasswordRequestDto req = new ForgotPasswordRequestDto();
        req.setEmail("user@example.com");
        req.setDateOfBirth("2000-01-01");
        when(clientRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> authService.verifyForgotPassword(req));
    }

    @Test
    @DisplayName("register handles very long email and name")
    void testRegisterWithLongEmailAndName() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("a".repeat(300) + "@example.com");
        req.setName("b".repeat(300));
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        when(clientRepository.count()).thenReturn(0L);
        doNothing().when(clientRepository).insert(any(Client.class));
        doNothing().when(walletRepository).insert(any(Wallet.class));
        Client result = authService.register(req);
        assertNotNull(result);
        assertTrue(result.getEmail().startsWith("a"));
        assertTrue(result.getName().startsWith("b"));
    }

    // --- FMTS API Tests ---
    @Test
    @DisplayName("getFmtsResponse returns token and clientId from valid FMTS JSON response")
    void testGetFmtsResponseValidJson() {
        Client client = new Client();
        client.setEmail("user@example.com");
        String fmtsJson = "{\"token\":\"tok123\",\"clientId\":\"fmts1\"}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate, passwordEncoder);
        var result = service.getFmtsResponse(client);
        assertEquals("tok123", result.getFmtsToken());
        assertEquals("fmts1", result.getFmtsClientId());
    }

    @Test
    @DisplayName("getFmtsResponse returns nulls for non-JSON FMTS response")
    void testGetFmtsResponseNonJson() {
        Client client = new Client();
        client.setEmail("user@example.com");
        String fmtsText = "Not JSON";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsText);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        var result = service.getFmtsResponse(client);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
    }

    @Test
    @DisplayName("getFmtsResponse returns nulls for FMTS exception")
    void testGetFmtsResponseException() {
        Client client = new Client();
        client.setEmail("user@example.com");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("FMTS error"));
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        var result = service.getFmtsResponse(client);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
    }

    @Test
    @DisplayName("getFmtsResponse returns nulls for FMTS JSON missing fields")
    void testGetFmtsResponseMissingFields() {
        Client client = new Client();
        client.setEmail("user@example.com");
        String fmtsJson = "{\"other\":\"value\"}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        var result = service.getFmtsResponse(client);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
    }

    @Test
    @DisplayName("registerWithFmts returns RegisterResponse with FMTS fields")
    void testRegisterWithFmtsHappyPath() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("user@example.com");
        req.setName("Test");
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        Client client = new Client();
        client.setEmail("user@example.com");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        when(clientRepository.count()).thenReturn(0L);
        doNothing().when(clientRepository).insert(any(Client.class));
        doNothing().when(walletRepository).insert(any(Wallet.class));
        String fmtsJson = "{\"token\":\"tok123\",\"clientId\":\"fmts1\"}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        when(passwordEncoder.encode("password")).thenReturn("password");
        var result = service.registerWithFmts(req);
        assertEquals("tok123", result.getFmtsToken());
        assertEquals("fmts1", result.getFmtsClientId());
        assertEquals("user@example.com", result.getClient().getEmail());
    }

    @Test
    @DisplayName("registerWithFmts returns RegisterResponse with null FMTS fields on FMTS error")
    void testRegisterWithFmtsFmtsError() {
        RegistrationRequest req = new RegistrationRequest();
        req.setEmail("user@example.com");
        req.setName("Test");
        req.setPassword("password");
        req.setIdentification(new HashSet<>());
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clientIdentificationRepository.countByValueIn(anyList())).thenReturn(0L);
        when(clientRepository.count()).thenReturn(0L);
        doNothing().when(clientRepository).insert(any(Client.class));
        doNothing().when(walletRepository).insert(any(Wallet.class));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("FMTS error"));
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        when(passwordEncoder.encode("password")).thenReturn("password");
        var result = service.registerWithFmts(req);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
        assertEquals("user@example.com", result.getClient().getEmail());
    }

    @Test
    @DisplayName("signInWithFmts returns SignInResponse with FMTS fields")
    void testSignInWithFmtsHappyPath() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("password");
        when(clientRepository.findByEmail("user@example.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        String fmtsJson = "{\"token\":\"tok123\",\"clientId\":\"fmts1\"}";
        ResponseEntity<String> response = ResponseEntity.ok(fmtsJson);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(response);
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        var result = service.signInWithFmts(req);
        assertEquals("tok123", result.getFmtsToken());
        assertEquals("fmts1", result.getFmtsClientId());
        assertEquals("user@example.com", result.getClient().getEmail());
    }

    @Test
    @DisplayName("signInWithFmts returns SignInResponse with null FMTS fields on FMTS error")
    void testSignInWithFmtsFmtsError() {
        SignInRequest req = new SignInRequest();
        req.setEmail("user@example.com");
        req.setPassword("password");
        Client client = new Client();
        client.setEmail("user@example.com");
        client.setPassword("password");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("FMTS error"));
        AuthService service = new AuthService(clientRepository, walletRepository, clientIdentificationRepository, restTemplate,passwordEncoder);
        var result = service.signInWithFmts(req);
        assertNull(result.getFmtsToken());
        assertNull(result.getFmtsClientId());
        assertEquals("user@example.com", result.getClient().getEmail());
    }
}
