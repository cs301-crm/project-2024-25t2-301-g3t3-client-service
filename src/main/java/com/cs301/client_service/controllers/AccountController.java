package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.services.impl.AccountServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountServiceImpl accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountServiceImpl accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            var accountModel = accountMapper.toModel(accountDTO);
            var savedAccount = accountService.createAccount(accountModel);
            return ResponseEntity.ok(accountMapper.toDto(savedAccount));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountId) {
        try {
            var account = accountService.getAccount(accountId);
            return ResponseEntity.ok(accountMapper.toDto(account));
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve account", e);
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable String accountId,
            @Valid @RequestBody AccountDTO accountDTO) {
        try {
            var accountModel = accountMapper.toModel(accountDTO);
            var updatedAccount = accountService.updateAccount(accountId, accountModel);
            return ResponseEntity.ok(accountMapper.toDto(updatedAccount));
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update account", e);
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        try {
            accountService.deleteAccount(accountId);
            return ResponseEntity.noContent().build();
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete account", e);
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable String clientId) {
        try {
            var accounts = accountService.getAccountsByClientId(clientId);
            return ResponseEntity.ok(accountMapper.toDtoList(accounts));
        } catch (ClientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve accounts for client", e);
        }
    }
}
