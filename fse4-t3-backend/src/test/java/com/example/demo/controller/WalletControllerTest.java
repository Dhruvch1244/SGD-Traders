package com.example.demo.controller;

import com.example.demo.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.demo.models.Wallet;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(WalletController.class)
@Import(TestSecurityConfig.class)
class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WalletService walletService;
    @Test
    void getWallet_success() throws Exception {
    when(walletService.getWalletByClientId("test-client"))
        .thenReturn(Optional.of(new Wallet()));
    mockMvc.perform(get("/api/wallets/test-client"))
        .andExpect(status().isOk());
    }
}
