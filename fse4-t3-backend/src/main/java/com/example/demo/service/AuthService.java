package com.example.demo.service;

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
import com.example.demo.models.Client;
import com.example.demo.models.ClientIdentification;
import com.example.demo.models.Wallet;
import com.example.demo.repository.ClientIdentificationRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private final ClientRepository clientRepository;
    private final WalletRepository walletRepository;
    private final ClientIdentificationRepository clientIdentificationRepository;
    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${fmts.url}")
    private String fmtsUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(ClientRepository clientRepository, WalletRepository walletRepository, ClientIdentificationRepository clientIdentificationRepository, RestTemplate restTemplate, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.walletRepository = walletRepository;
        this.clientIdentificationRepository = clientIdentificationRepository;
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public SignInResponse getFmtsResponse(Client client) {
        // String fmtsUrl = "http://localhost:3000/fmts/client"; // replaced by injected property
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        MultiValueMap<String, String> fmtsPayload = new LinkedMultiValueMap<>();
        fmtsPayload.add("email", client.getEmail());
        fmtsPayload.add("clientId", "");
        logger.info("FMTS API request (form): url={}, headers={}, payload={}", fmtsUrl, headers, fmtsPayload);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(fmtsPayload, headers);
        String fmtsToken = null;
        String fmtsClientId = null;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fmtsUrl, entity, String.class);
            logger.info("FMTS API raw response: status={}, body={}", response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody();
                if (body.trim().startsWith("{")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    try {
                        Map<String, Object> map = mapper.readValue(body, Map.class);
                        Object tokenObj = map.get("token");
                        Object clientIdObj = map.get("clientId");
                        fmtsToken = tokenObj != null ? tokenObj.toString() : null;
                        fmtsClientId = clientIdObj != null ? clientIdObj.toString() : null;
                    } catch (Exception jsonEx) {
                        logger.warn("FMTS response not valid JSON: {}", jsonEx.getMessage());
                    }
                } else {
                    logger.warn("FMTS response not JSON: {}", body);
                }
            } else {
                logger.warn("FMTS API did not return expected fields. Response: {}", response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error calling FMTS API: {}", e.getMessage(), e);
            fmtsToken = null;
            fmtsClientId = null;
        }
        return new SignInResponse(client, fmtsToken, fmtsClientId);
    }

    // Legacy-compatible sign-in returning Client for tests and existing code
    public Client signIn(SignInRequest signInRequest) {
        logger.info("Attempting to sign in user with email: {}", signInRequest.getEmail());
        if (signInRequest == null) {
            throw new InvalidRequestException("SignInRequest cannot be null");
        }
        if (signInRequest.getEmail() == null || signInRequest.getEmail().isBlank()) {
            throw new InvalidRequestException("Email cannot be null or empty");
        }
        if (signInRequest.getPassword() == null || signInRequest.getPassword().isBlank()) {
            throw new InvalidRequestException("Password cannot be null or empty");
        }

        Client client = clientRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Client with email " + signInRequest.getEmail() + " not found"));

        if (!passwordEncoder.matches(signInRequest.getPassword(), client.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        logger.info("User with email {} signed in successfully", signInRequest.getEmail());
        return client;
    }

    // New method with FMTS fields for modern callers
    public SignInResponse signInWithFmts(SignInRequest signInRequest) {
        Client client = signIn(signInRequest);
        return getFmtsResponse(client);
    }

    @Transactional
    // Legacy-compatible register returning Client for tests and existing code
    public Client register(RegistrationRequest registrationRequest) {
        logger.info("Attempting to register new user with email: {}", registrationRequest.getEmail());
        if (registrationRequest == null) {
            throw new InvalidRequestException("Registration request cannot be null");
        }
        if (registrationRequest.getEmail() == null || registrationRequest.getEmail().isBlank()) {
            throw new InvalidRequestException("Email cannot be null or empty");
        }
        if (registrationRequest.getPassword() == null || registrationRequest.getPassword().isBlank()) {
            throw new InvalidRequestException("Password cannot be null or empty");
        }
        if (registrationRequest.getName() == null || registrationRequest.getName().isBlank()) {
            throw new InvalidRequestException("Name cannot be null or empty");
        }
        if (registrationRequest.getIdentification() == null) {
            throw new InvalidRequestException("Identification set cannot be null");
        }

        // Validate email uniqueness
        if (emailExists(registrationRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }
        // Validate identification uniqueness
        if (identificationExists(registrationRequest.getIdentification())) {
            throw new ResourceConflictException("Client details already exist");
        }

        // Create new client
        Client newClient = new Client();
        newClient.setClientId(generateClientId());
        newClient.setName(registrationRequest.getName());
        newClient.setEmail(registrationRequest.getEmail());
        newClient.setDateOfBirth(normalizeDate(registrationRequest.getDateOfBirth()));
        newClient.setCountry(registrationRequest.getCountry());
        newClient.setPostalCode(registrationRequest.getPostalCode());
        newClient.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newClient.setNew(true);

        // Save client to database
        clientRepository.insert(newClient);

        // Save client identifications
        Set<ClientIdentification> identifications = registrationRequest.getIdentification();
        if (!identifications.isEmpty()) {
            identifications.forEach(id -> {
                id.setId(UUID.randomUUID().toString());
                id.setClientId(newClient.getClientId());
                clientIdentificationRepository.insert(id);
            });
        }

        // Create wallet for the new client
        Wallet newWallet = new Wallet();
        newWallet.setClientId(newClient.getClientId());
        newWallet.setBalance(0.0);
        walletRepository.insert(newWallet);
        logger.info("User with email {} registered successfully", registrationRequest.getEmail());
        return newClient;
    }

    // New method that returns RegisterResponse with FMTS fields
    public RegisterResponse registerWithFmts(RegistrationRequest registrationRequest) {
        Client client = register(registrationRequest);
        SignInResponse fmts = getFmtsResponse(client);
        return new RegisterResponse(client, fmts.getFmtsToken(), fmts.getFmtsClientId());
    }

    public void verifyForgotPassword(ForgotPasswordRequestDto request) {
        if (request == null) {
            throw new InvalidRequestException("Request cannot be null");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("Email cannot be null or empty");
        }
        if (request.getDateOfBirth() == null || request.getDateOfBirth().isBlank()) {
            throw new InvalidRequestException("Date of birth cannot be null or empty");
        }

        String requestDob = normalizeDate(request.getDateOfBirth());
        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Client with email " + request.getEmail() + " not found"));

        if (!safeDatesEqual(client.getDateOfBirth(), requestDob)) {
            throw new AuthenticationException("Date of birth does not match");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {
        if (request == null) {
            throw new InvalidRequestException("Request cannot be null");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidRequestException("Email cannot be null or empty");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new InvalidRequestException("New password cannot be null or empty");
        }

        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Client with email " + request.getEmail() + " not found"));

        client.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.update(client);
    }

    private boolean emailExists(String email) {
        return clientRepository.findByEmail(email).isPresent();
    }

    private boolean identificationExists(Set<ClientIdentification> identifications) {
        if (identifications == null || identifications.isEmpty()) {
            return false;
        }
        List<String> newValues = identifications.stream().map(ClientIdentification::getValue).collect(Collectors.toList());
        return clientIdentificationRepository.countByValueIn(newValues) > 0;
    }

    private String generateClientId() {
        return "C" + (clientRepository.count() + 1);
    }

    // ---- Date helpers ----
    private static final DateTimeFormatter OUTPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter[] INPUT_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private String normalizeDate(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        for (DateTimeFormatter fmt : INPUT_FORMATS) {
            try {
                LocalDate d = LocalDate.parse(input, fmt);
                return d.format(OUTPUT_FMT);
            } catch (DateTimeParseException ignored) {}
        }
        // Fallback: return as-is if parsing fails
        return input;
    }

    private boolean safeDatesEqual(String dbValue, String normalizedRequest) {
        if (dbValue == null && normalizedRequest == null) return true;
        if (dbValue == null || normalizedRequest == null) return false;
        String normalizedDb = normalizeDate(dbValue);
        return normalizedDb.equals(normalizedRequest);
    }
}
