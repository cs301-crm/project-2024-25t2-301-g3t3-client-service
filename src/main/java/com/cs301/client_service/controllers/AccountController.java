package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ApiException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.services.impl.AccountServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountServiceImpl accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountServiceImpl accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        logger.info("AccountController initialized");
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            logger.debug("Received create account request: {}", accountDTO);
            var accountModel = accountMapper.toModel(accountDTO);
            logger.debug("Mapped to model: {}", accountModel);
            var savedAccount = accountService.createAccount(accountModel);
            logger.debug("Saved account: {}", savedAccount);
            var response = accountMapper.toDto(savedAccount);
            logger.debug("Returning response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating account: ", e);
            throw new ApiException("Failed to create account");
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountId) {
        try {
            logger.debug("Received get account request for id: {}", accountId);
            var account = accountService.getAccount(accountId);
            logger.debug("Retrieved account: {}", account);
            var response = accountMapper.toDto(account);
            logger.debug("Returning response: {}", response);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            logger.error("Account not found: {}", accountId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving account: ", e);
            throw new ApiException("Failed to retrieve account");
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable String accountId,
            @Valid @RequestBody AccountDTO accountDTO) {
        try {
            logger.debug("Received update account request for id: {} with data: {}", accountId, accountDTO);
            var accountModel = accountMapper.toModel(accountDTO);
            logger.debug("Mapped to model: {}", accountModel);
            var updatedAccount = accountService.updateAccount(accountId, accountModel);
            logger.debug("Updated account: {}", updatedAccount);
            var response = accountMapper.toDto(updatedAccount);
            logger.debug("Returning response: {}", response);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            logger.error("Account not found: {}", accountId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating account: ", e);
            throw new ApiException("Failed to update account");
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        try {
            logger.debug("Received delete account request for id: {}", accountId);
            accountService.deleteAccount(accountId);
            logger.debug("Account deleted successfully: {}", accountId);
            return ResponseEntity.noContent().build();
        } catch (AccountNotFoundException e) {
            logger.error("Account not found: {}", accountId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting account: ", e);
            throw new ApiException("Failed to delete account");
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable String clientId) {
        try {
            logger.debug("Received get accounts by client id request: {}", clientId);
            var accounts = accountService.getAccountsByClientId(clientId);
            logger.debug("Retrieved {} accounts for client {}", accounts.size(), clientId);
            var response = accountMapper.toDtoList(accounts);
            logger.debug("Returning response with {} accounts", response.size());
            return ResponseEntity.ok(response);
        } catch (ClientNotFoundException e) {
            logger.error("Client not found: {}", clientId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving accounts for client: ", e);
            throw new ApiException("Failed to retrieve accounts for client");
        }
    }
}
