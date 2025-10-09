package com.example.demo.controller;

import com.example.demo.service.TradeExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(TradeExecutionController.class)
@Import(TestSecurityConfig.class)
class TradeExecutionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TradeExecutionService tradeExecutionService;
    @Test
    void executeTrade_success() throws Exception {
        mockMvc.perform(post("/api/trade/buy")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }
}
