package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import com.example.demo.repository.ClientRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(TestController.class)
@Import(TestSecurityConfig.class)
class TestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClientRepository clientRepository;
    @Test
    void testEndpoint_success() throws Exception {
        mockMvc.perform(get("/api/test/database"))
                .andExpect(status().isOk());
    }
}
