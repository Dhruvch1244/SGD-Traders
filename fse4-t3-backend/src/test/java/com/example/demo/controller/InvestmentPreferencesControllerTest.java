package com.example.demo.controller;

import com.example.demo.service.InvestmentPreferencesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.demo.models.InvestmentPreferences;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(InvestmentPreferencesController.class)
@Import(TestSecurityConfig.class)
class InvestmentPreferencesControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InvestmentPreferencesService investmentPreferencesService;
    @Test
    void getPreferences_success() throws Exception {
    when(investmentPreferencesService.getPreferences("test-client"))
        .thenReturn(Optional.of(new InvestmentPreferences()));
    mockMvc.perform(get("/api/preferences/test-client"))
        .andExpect(status().isOk());
    }
}
