package com.example.demo.controller;

import com.example.demo.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(MarketDataController.class)
@Import(TestSecurityConfig.class)
class MarketDataControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MarketDataService marketDataService;
    @Test
    void getMarketData_success() throws Exception {
        mockMvc.perform(get("/api/market/instruments"))
                .andExpect(status().isOk());
    }
}
