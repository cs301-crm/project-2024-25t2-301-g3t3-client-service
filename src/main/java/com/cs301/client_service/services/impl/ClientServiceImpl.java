package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.AccountService;
import com.cs301.client_service.services.ClientService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final AccountService accountService;
    
    public ClientServiceImpl(ClientRepository clientRepository, AccountService accountService) {
        this.clientRepository = clientRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClient(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getClientsByAgentId(String agentId) {
        return clientRepository.findByAgentId(agentId);
    }

    @Override
    @Transactional
    public Client updateClient(String clientId, Client client) {
        // Verify client exists
        validateClientOperation(clientId, "update");

        client.setClientId(clientId); // Ensure ID matches
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deleteClient(String clientId) {
        // Verify client exists and has no active accounts
        validateClientOperation(clientId, "delete");

        // Delete all associated accounts first
        accountService.deleteAccountsByClientId(clientId);

        // Then delete the client
        clientRepository.deleteById(clientId);
    }

    @Override
    public void verifyClient(String clientId, String nric) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (!client.getNric().equals(nric)) {
            throw new VerificationException("Invalid NRIC provided");
        }
    }

    private void validateClientOperation(String clientId, String operation) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (operation.equals("delete")) {
            // Check if client has any active accounts before deletion
            List<Account> accounts = accountService.getAccountsByClientId(clientId);
            boolean hasActiveAccounts = accounts.stream()
                    .anyMatch(account -> account.getAccountStatus() == AccountStatus.ACTIVE);

            if (hasActiveAccounts) {
                throw new VerificationException("Cannot delete client with active accounts");
            }
        }
    }
}
