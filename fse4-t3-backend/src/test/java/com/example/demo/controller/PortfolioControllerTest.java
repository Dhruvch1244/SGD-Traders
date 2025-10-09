package com.example.demo.controller;

import com.example.demo.service.PortfolioService;
import com.example.demo.service.PortfolioDetailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(PortfolioController.class)
@Import(TestSecurityConfig.class)
class PortfolioControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PortfolioService portfolioService;
    @MockBean
    private PortfolioDetailService portfolioDetailService;
    @Test
    void getPortfolio_success() throws Exception {
        mockMvc.perform(get("/api/portfolios/test-client"))
                .andExpect(status().isOk());
    }
}
