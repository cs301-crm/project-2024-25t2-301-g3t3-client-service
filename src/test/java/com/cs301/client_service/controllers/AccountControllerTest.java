package com.cs301.client_service.controllers;

import com.cs301.client_service.configs.TestSecurityConfig;
import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AccountControllerTest {

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
        String nonExistentId = "non-existent-id";
        when(accountService.getAccount(anyString())).thenThrow(new AccountNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/{accountId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccount(nonExistentId);
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
        String nonExistentId = "non-existent-id";
        doThrow(new AccountNotFoundException(nonExistentId))
                .when(accountService).deleteAccount(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/v1/accounts/{accountId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).deleteAccount(nonExistentId);
    }
    
    @Test
    void testGetAccountsByClientId_Success() throws Exception {
        // Given
        List<Account> accounts = Arrays.asList(accountModel);
        List<AccountDTO> accountDTOs = Arrays.asList(accountDTO);
        
        when(accountService.getAccountsByClientId(clientId)).thenReturn(accounts);
        when(accountMapper.toDtoList(accounts)).thenReturn(accountDTOs);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId", is(accountId)))
                .andExpect(jsonPath("$[0].clientId", is(clientId)))
                .andExpect(jsonPath("$[0].accountType", is(AccountType.SAVINGS.toString())))
                .andExpect(jsonPath("$[0].currency", is("SGD")));

        verify(accountService, times(1)).getAccountsByClientId(clientId);
        verify(accountMapper, times(1)).toDtoList(accounts);
    }

    @Test
    void testGetAccountsByClientId_ClientNotFound() throws Exception {
        // Given
        String nonExistentClient = "non-existent-client";
        when(accountService.getAccountsByClientId(nonExistentClient))
                .thenThrow(new ClientNotFoundException(nonExistentClient));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/client/{clientId}", nonExistentClient))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountsByClientId(nonExistentClient);
        verify(accountMapper, never()).toDtoList(any());
    }
}
