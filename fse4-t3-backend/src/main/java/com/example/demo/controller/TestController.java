package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.models.Client;
import com.example.demo.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final ClientRepository clientRepository;

    public TestController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping("/database")
    public ResponseEntity<ApiResponse> testDatabase() {
        try {
            // Test basic connection
            long clientCount = clientRepository.count();
            
            Map<String, Object> data = new HashMap<>();
            data.put("connection", "success");
            data.put("clientCount", clientCount);
            
            return ResponseEntity.ok(new ApiResponse(true, "Database connection successful", data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse(false, "Database connection failed: " + e.getMessage()));
        }
    }

    @PostMapping("/client")
    public ResponseEntity<ApiResponse> testClientInsert() {
        try {
            // Create a test client
            Client testClient = new Client();
            String clientId = "TEST" + System.currentTimeMillis();
            testClient.setClientId(clientId);
            testClient.setName("Test User");
            testClient.setEmail("test" + System.currentTimeMillis() + "@example.com");
            testClient.setDateOfBirth("01-01-1990");
            testClient.setCountry("USA");
            testClient.setPostalCode("12345");
            testClient.setPassword("password123");

            // Save the client
            clientRepository.insert(testClient);
            
            // Clean up
            clientRepository.deleteById(clientId);
            
            return ResponseEntity.ok(new ApiResponse(true, "Client insert/delete test successful", testClient));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse(false, "Client test failed: " + e.getMessage()));
        }
    }
}
