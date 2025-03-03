package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ApiException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.ErrorResponse;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.services.impl.AccountServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            logger.debug("Received create account request");
            var accountModel = accountMapper.toModel(accountDTO);
            logger.debug("Account model mapped successfully");
            var savedAccount = accountService.createAccount(accountModel);
            logger.debug("Account created successfully");
            var response = accountMapper.toDto(savedAccount);
            logger.debug("Response mapped successfully");
            return ResponseEntity.ok(response);
        } catch (ClientNotFoundException e) {
            logger.error("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating account: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to create account: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable String accountId) {
        try {
            logger.debug("Received get account request");
            var account = accountService.getAccount(accountId);
            logger.debug("Account retrieved successfully");
            var response = accountMapper.toDto(account);
            logger.debug("Response mapped successfully");
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            logger.error("Account not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error retrieving account: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to retrieve account: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<?> updateAccount(
            @PathVariable String accountId,
            @Valid @RequestBody AccountDTO accountDTO) {
        try {
            logger.debug("Received update account request");
            var accountModel = accountMapper.toModel(accountDTO);
            logger.debug("Account model mapped successfully");
            var updatedAccount = accountService.updateAccount(accountId, accountModel);
            logger.debug("Account updated successfully");
            var response = accountMapper.toDto(updatedAccount);
            logger.debug("Response mapped successfully");
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            logger.error("Account not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ClientNotFoundException e) {
            logger.error("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating account: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to update account: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable String accountId) {
        try {
            logger.debug("Received delete account request");
            accountService.deleteAccount(accountId);
            logger.debug("Account deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (AccountNotFoundException e) {
            logger.error("Account not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error deleting account: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to delete account: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getAccountsByClientId(@PathVariable String clientId) {
        try {
            logger.debug("Received get accounts by client id request");
            var accounts = accountService.getAccountsByClientId(clientId);
            logger.debug("Retrieved {} accounts", accounts.size());
            var response = accountMapper.toDtoList(accounts);
            logger.debug("Response mapped successfully with {} accounts", response.size());
            return ResponseEntity.ok(response);
        } catch (ClientNotFoundException e) {
            logger.error("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error retrieving accounts for client: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to retrieve accounts for client: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
