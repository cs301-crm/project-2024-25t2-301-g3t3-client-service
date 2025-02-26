package com.cs301.client_service.services.impl;

import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.AccountService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        // Verify client exists
        Client client = clientRepository.findById(account.getClient().getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + account.getClient().getClientId()));

        account.setClient(client);
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByClientId(String clientId) {
        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client not found with ID: " + clientId);
        }

        return accountRepository.findByClientClientId(clientId);
    }

    @Override
    @Transactional
    public Account updateAccount(String accountId, Account account) {
        // Verify account exists
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        // Verify client exists
        Client client = clientRepository.findById(account.getClient().getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found with ID: " + account.getClient().getClientId()));

        account.setAccountId(accountId); // Ensure ID matches
        account.setClient(client);

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(String accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }
        accountRepository.deleteById(accountId);
    }

    @Override
    @Transactional
    public void deleteAccountsByClientId(String clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException("Client not found with ID: " + clientId);
        }
        accountRepository.deleteByClientClientId(clientId);
    }
}