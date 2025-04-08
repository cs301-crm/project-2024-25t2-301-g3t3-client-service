package com.cs301.client_service.controllers;

import com.cs301.client_service.configs.TestSecurityConfig;
import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.dtos.PaginatedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Disabled for unit testing")
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
    void testGetAllAccounts_Success() throws Exception {
        // Given
        int page = 1;
        int limit = 10;
        AccountType type = AccountType.SAVINGS;
        AccountStatus status = AccountStatus.ACTIVE;
        
        Account anotherAccount = new Account();
        anotherAccount.setAccountId("another-account-uuid");
        anotherAccount.setAccountType(AccountType.SAVINGS);
        anotherAccount.setAccountStatus(AccountStatus.ACTIVE);
        
        Client client = new Client();
        client.setClientId(clientId);
        anotherAccount.setClient(client);
        
        List<Account> accounts = Arrays.asList(accountModel, anotherAccount);
        Page<Account> accountPage = new PageImpl<>(accounts, PageRequest.of(page - 1, limit), 2);
        
        AccountDTO anotherAccountDTO = new AccountDTO();
        anotherAccountDTO.setAccountId("another-account-uuid");
        anotherAccountDTO.setClientId(clientId);
        anotherAccountDTO.setAccountType(AccountType.SAVINGS);
        anotherAccountDTO.setAccountStatus(AccountStatus.ACTIVE);
        
        List<AccountDTO> accountDTOs = Arrays.asList(accountDTO, anotherAccountDTO);
        
        when(accountService.getAllAccountsPaginated(any(Pageable.class), eq(type), eq(status))).thenReturn(accountPage);
        when(accountMapper.toDtoList(accounts)).thenReturn(accountDTOs);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts")
                .param("page", String.valueOf(page))
                .param("limit", String.valueOf(limit))
                .param("type", type.toString())
                .param("status", status.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].accountId", is(accountId)))
                .andExpect(jsonPath("$.data[0].clientId", is(clientId)))
                .andExpect(jsonPath("$.data[0].accountType", is(AccountType.SAVINGS.toString())))
                .andExpect(jsonPath("$.data[1].accountId", is("another-account-uuid")))
                .andExpect(jsonPath("$.page", is(page)))
                .andExpect(jsonPath("$.limit", is(limit)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(accountService, times(1)).getAllAccountsPaginated(any(Pageable.class), eq(type), eq(status));
        verify(accountMapper, times(1)).toDtoList(accounts);
    }
    
    @Test
    void testGetAccountsByClientId_Success() throws Exception {
        // Given
        int page = 1;
        int limit = 10;
        
        List<Account> accounts = Arrays.asList(accountModel);
        Page<Account> accountPage = new PageImpl<>(accounts, PageRequest.of(page - 1, limit), 1);
        
        List<AccountDTO> accountDTOs = Arrays.asList(accountDTO);
        
        when(accountService.getAccountsByClientIdPaginated(eq(clientId), any(Pageable.class))).thenReturn(accountPage);
        when(accountMapper.toDtoList(accounts)).thenReturn(accountDTOs);

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/client/{clientId}", clientId)
                .param("page", String.valueOf(page))
                .param("limit", String.valueOf(limit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].accountId", is(accountId)))
                .andExpect(jsonPath("$.data[0].clientId", is(clientId)))
                .andExpect(jsonPath("$.data[0].accountType", is(AccountType.SAVINGS.toString())))
                .andExpect(jsonPath("$.data[0].currency", is("SGD")))
                .andExpect(jsonPath("$.page", is(page)))
                .andExpect(jsonPath("$.limit", is(limit)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));

        verify(accountService, times(1)).getAccountsByClientIdPaginated(eq(clientId), any(Pageable.class));
        verify(accountMapper, times(1)).toDtoList(accounts);
    }

    @Test
    void testGetAccountsByClientId_ClientNotFound() throws Exception {
        // Given
        int page = 1;
        int limit = 10;
        String nonExistentClient = "non-existent-client";
        
        when(accountService.getAccountsByClientIdPaginated(eq(nonExistentClient), any(Pageable.class)))
                .thenThrow(new ClientNotFoundException(nonExistentClient));

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/client/{clientId}", nonExistentClient)
                .param("page", String.valueOf(page))
                .param("limit", String.valueOf(limit)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountsByClientIdPaginated(eq(nonExistentClient), any(Pageable.class));
        verify(accountMapper, never()).toDtoList(any());
    }
}
