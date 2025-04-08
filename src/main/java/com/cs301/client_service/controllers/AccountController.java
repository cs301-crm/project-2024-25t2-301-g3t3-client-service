package com.cs301.client_service.controllers;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.services.AccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
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

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) AccountStatus status) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Account> accountsPage;
        
        if ((agentId != null && !agentId.isEmpty()) || (searchQuery != null && !searchQuery.isEmpty())) {
            // If agentId or searchQuery is provided, use the combined search and filters method
            accountsPage = accountService.getAccountsWithSearchAndFilters(agentId, searchQuery, type, status, pageable);
        } else {
            // Otherwise, use the existing method for filtering by type and status
            accountsPage = accountService.getAllAccountsPaginated(pageable, type, status);
        }
        
        List<AccountDTO> accountDTOs = accountMapper.toDtoList(accountsPage.getContent());
        
        return ResponseEntity.ok(accountDTOs);
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
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Account> accountsPage = accountService.getAccountsByClientIdPaginated(clientId, pageable);
        
        List<AccountDTO> accountDTOs = accountMapper.toDtoList(accountsPage.getContent());
        
        return ResponseEntity.ok(accountDTOs);
    }
}
