package com.example.demo.controller;

import com.example.demo.service.TradeService;
import com.example.demo.service.InstrumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(TradeController.class)
@Import(TestSecurityConfig.class)
class TradeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TradeService tradeService;
    @MockBean
    private InstrumentService instrumentService;
    @Test
    void getTrades_success() throws Exception {
        mockMvc.perform(get("/api/trades/test-client"))
                .andExpect(status().isOk());
    }
}
