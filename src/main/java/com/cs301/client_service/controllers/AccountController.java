package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.services.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        logger.info("AccountController initialized");
    }

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        var accountModel = accountMapper.toModel(accountDTO);
        var savedAccount = accountService.createAccount(accountModel);
        var response = accountMapper.toDto(savedAccount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountId) {
        var account = accountService.getAccount(accountId);
        var response = accountMapper.toDto(account);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable String clientId) {
        var accounts = accountService.getAccountsByClientId(clientId);
        var response = accountMapper.toDtoList(accounts);
        return ResponseEntity.ok(response);
    }
}
