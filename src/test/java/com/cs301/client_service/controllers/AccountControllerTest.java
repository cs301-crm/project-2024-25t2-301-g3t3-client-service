package com.cs301.client_service.controllers;

import com.cs301.client_service.configs.TestSecurityConfig;
import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.services.impl.AccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountServiceImpl accountService;

    @MockBean
    private AccountMapper accountMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDTO accountDTO;
    private Account accountModel;
    private String accountId = "account-uuid";
    private String clientId = "client-uuid";

    @BeforeEach
    void setUp() {
        // Setup client
        Client client = new Client();
        client.setClientId(clientId);

        // Setup account model
        accountModel = new Account();
        accountModel.setAccountId(accountId);
        accountModel.setClient(client);
        accountModel.setAccountType(AccountType.SAVINGS);
        accountModel.setAccountStatus(AccountStatus.ACTIVE);
        accountModel.setOpeningDate(LocalDate.now());
        accountModel.setInitialDeposit(new BigDecimal("1000.00"));
        accountModel.setCurrency("SGD");
        accountModel.setBranchId("BR001");

        // Setup account DTO
        accountDTO = new AccountDTO();
        accountDTO.setAccountId(accountId);
        accountDTO.setClientId(clientId);
        accountDTO.setAccountType(AccountType.SAVINGS);
        accountDTO.setAccountStatus(AccountStatus.ACTIVE);
        accountDTO.setOpeningDate(LocalDate.now().toString());
        accountDTO.setInitialDeposit(new BigDecimal("1000.00"));
        accountDTO.setCurrency("SGD");
        accountDTO.setBranchId("BR001");
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Given
        when(accountMapper.toModel(any(AccountDTO.class))).thenReturn(accountModel);
        when(accountService.createAccount(any(Account.class))).thenReturn(accountModel);
        when(accountMapper.toDto(any(Account.class))).thenReturn(accountDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.clientId", is(clientId)))
                .andExpect(jsonPath("$.accountType", is(AccountType.SAVINGS.toString())))
                .andExpect(jsonPath("$.currency", is("SGD")));

        verify(accountMapper, times(1)).toModel(any(AccountDTO.class));
        verify(accountService, times(1)).createAccount(any(Account.class));
        verify(accountMapper, times(1)).toDto(any(Account.class));
    }

    @Test
    void testGetAccount_Success() throws Exception {
        // Given
        when(accountService.getAccount(accountId)).thenReturn(accountModel);
        when(accountMapper.toDto(any(Account.class))).thenReturn(accountDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.clientId", is(clientId)));

        verify(accountService, times(1)).getAccount(accountId);
        verify(accountMapper, times(1)).toDto(accountModel);
    }

    @Test
    void testGetAccount_NotFound() throws Exception {
        // Given
        when(accountService.getAccount(anyString())).thenThrow(new AccountNotFoundException("Account not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/{accountId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccount("non-existent-id");
        verify(accountMapper, never()).toDto(any(Account.class));
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        // Given
        when(accountMapper.toModel(any(AccountDTO.class))).thenReturn(accountModel);
        when(accountService.updateAccount(eq(accountId), any(Account.class))).thenReturn(accountModel);
        when(accountMapper.toDto(any(Account.class))).thenReturn(accountDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.clientId", is(clientId)));

        verify(accountMapper, times(1)).toModel(any(AccountDTO.class));
        verify(accountService, times(1)).updateAccount(eq(accountId), any(Account.class));
        verify(accountMapper, times(1)).toDto(any(Account.class));
    }

    @Test
    void testUpdateAccount_NotFound() throws Exception {
        // Given
        when(accountMapper.toModel(any(AccountDTO.class))).thenReturn(accountModel);
        when(accountService.updateAccount(anyString(), any(Account.class)))
                .thenThrow(new AccountNotFoundException("Account not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/accounts/{accountId}", "non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isNotFound());

        verify(accountMapper, times(1)).toModel(any(AccountDTO.class));
        verify(accountService, times(1)).updateAccount(eq("non-existent-id"), any(Account.class));
        verify(accountMapper, never()).toDto(any(Account.class));
    }

    @Test
    void testDeleteAccount_Success() throws Exception {
        // Given
        doNothing().when(accountService).deleteAccount(accountId);

        // When & Then
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(accountId);
    }

    @Test
    void testDeleteAccount_NotFound() throws Exception {
        // Given
        doThrow(new AccountNotFoundException("Account not found"))
                .when(accountService).deleteAccount("non-existent-id");

        // When & Then
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).deleteAccount("non-existent-id");
    }
}