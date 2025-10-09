package com.example.demo.controller;

import com.example.demo.dto.SignInRequest;
import com.example.demo.dto.SignInResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.InvestmentPreferencesService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.example.demo.config.TestSecurityConfig;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private InvestmentPreferencesService investmentPreferencesService;
    @WithMockUser
    @Test
    void signIn_success() throws Exception {
        SignInRequest request = new SignInRequest();
        request.setEmail("user@example.com");
        request.setPassword("Pass123!");
        // Mock the client and response
        com.example.demo.models.Client client = new com.example.demo.models.Client();
        client.setClientId("client-123");
        SignInResponse response = new SignInResponse();
        response.setClient(client);
        Mockito.when(authService.signInWithFmts(Mockito.any())).thenReturn(response);
        Mockito.when(investmentPreferencesService.hasPreferences("client-123")).thenReturn(true);
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"Pass123!\"}"))
                .andExpect(status().isOk());
    }
}
