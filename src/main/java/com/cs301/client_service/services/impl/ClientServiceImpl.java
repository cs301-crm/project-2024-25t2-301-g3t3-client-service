package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.shared.protobuf.C2C;
import com.cs301.shared.protobuf.CRUDInfo;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.AccountService;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.ClientContextHolder;
import com.cs301.client_service.utils.LoggingUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
    private static final String CRUD_TYPE_UPDATE = "UPDATE";
    private static final String CRUD_TYPE_DELETE = "DELETE";
    private static final String OPERATION_UPDATE = "update";
    private static final String OPERATION_DELETE = "delete";
    private static final String OPERATION_VERIFY = "verify";
    
    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final KafkaProducer kafkaProducer;
    
    public ClientServiceImpl(ClientRepository clientRepository, AccountService accountService, KafkaProducer kafkaProducer) {
        this.clientRepository = clientRepository;
        this.accountService = accountService;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
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
    public Page<Client> getAllClientsPaginated(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return clientRepository.findAllWithSearch(search.trim(), pageable);
        }
        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getClientsByAgentId(String agentId) {
        return clientRepository.findByAgentId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsByAgentIdPaginated(String agentId, Pageable pageable) {
        return clientRepository.findByAgentId(agentId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsWithSearchAndAgentId(String agentId, String searchQuery, Pageable pageable) {
        return clientRepository.findWithSearchAndAgentId(agentId, searchQuery, pageable);
    }

    @Override
    public Client updateClient(String clientId, Client client) {
        logger.debug("SERVICE: Update client request received for clientId: {}", clientId);
        logger.debug("SERVICE: Client to update: {}", client);
        
        Client existingClient = validateClientOperation(clientId, OPERATION_UPDATE);
        logger.debug("SERVICE: Existing client from DB: {}", existingClient);
        
        // Log specific fields for easy comparison
        logger.debug("SERVICE: BEFORE UPDATE - FirstName: {}, LastName: {}, Email: {}, Phone: {}, Address: {}", 
            existingClient.getFirstName(), existingClient.getLastName(), 
            existingClient.getEmailAddress(), existingClient.getPhoneNumber(),
            existingClient.getAddress());
        
        logger.debug("SERVICE: UPDATE WITH - FirstName: {}, LastName: {}, Email: {}, Phone: {}, Address: {}", 
            client.getFirstName(), client.getLastName(), 
            client.getEmailAddress(), client.getPhoneNumber(),
            client.getAddress());
            
        String clientEmail = client.getEmailAddress();
        
        try {
            setClientContext(clientId, clientEmail);
            sendKafkaMessageSafely(() -> 
                sendClientUpdateKafkaMessage(clientId, clientEmail, existingClient, client),
                "client update"
            );
            
            client.setClientId(clientId);
            logger.debug("SERVICE: About to save updated client to DB: {}", client);
            
            Client savedClient = clientRepository.save(client);
            logger.debug("SERVICE: Client after save operation: {}", savedClient);
            
            // Log changes for key fields
            if (!existingClient.getFirstName().equals(savedClient.getFirstName())) {
                logger.info("SERVICE: First name changed: {} -> {}", 
                    existingClient.getFirstName(), savedClient.getFirstName());
            }
            if (!existingClient.getLastName().equals(savedClient.getLastName())) {
                logger.info("SERVICE: Last name changed: {} -> {}", 
                    existingClient.getLastName(), savedClient.getLastName());
            }
            if (!existingClient.getEmailAddress().equals(savedClient.getEmailAddress())) {
                logger.info("SERVICE: Email changed: {} -> {}", 
                    existingClient.getEmailAddress(), savedClient.getEmailAddress());
            }
            if (!existingClient.getPhoneNumber().equals(savedClient.getPhoneNumber())) {
                logger.info("SERVICE: Phone changed: {} -> {}", 
                    existingClient.getPhoneNumber(), savedClient.getPhoneNumber());
            }
            if (!existingClient.getAddress().equals(savedClient.getAddress())) {
                logger.info("SERVICE: Address changed: {} -> {}", 
                    existingClient.getAddress(), savedClient.getAddress());
            }
            
            return savedClient;
        } finally {
            ClientContextHolder.clear();
        }
    }

    @Override
    public void deleteClient(String clientId) {
        Client client = validateClientOperation(clientId, OPERATION_DELETE);
        String clientEmail = client.getEmailAddress();
        
        try {
            setClientContext(clientId, clientEmail);
            logger.info("Deleting client {} with email {}", clientId, clientEmail);
            
            sendKafkaMessageSafely(() -> 
                sendClientDeleteKafkaMessage(clientId, clientEmail),
                "client deletion"
            );
            
            deleteClientData(clientId);
        } finally {
            ClientContextHolder.clear();
        }
    }

    @Override
    public void verifyClient(String clientId) {
        Client client = validateClientOperation(clientId, OPERATION_VERIFY);
        client.setVerificationStatus(VerificationStatus.VERIFIED);
        clientRepository.save(client);
    }
    
    private void setClientContext(String clientId, String clientEmail) {
        ClientContextHolder.setClientId(clientId);
        ClientContextHolder.setClientEmail(clientEmail);
    }
    
    private void sendKafkaMessageSafely(Runnable messageSender, String operationType) {
        try {
            messageSender.run();
        } catch (Exception e) {
            logger.error("Error sending C2C message for {}: {}", operationType, e.getMessage(), e);
            // Continue with operation even if message sending fails
        }
    }
    
    private void deleteClientData(String clientId) {
        logger.info("Deleting associated accounts for client: {}", clientId);
        accountService.deleteAccountsByClientId(clientId);
        
        logger.info("Deleting the client: {}", clientId);
        clientRepository.deleteById(clientId);
        
        logger.info("Client deleted: {}", clientId);
    }
    
    private void sendClientUpdateKafkaMessage(String clientId, String clientEmail, Client existingClient, Client updatedClient) {
        logger.info("Sending C2C message for client update - clientId: {}", clientId);
        
        Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(existingClient, updatedClient);
        changes.remove("clientId");
        
        CRUDInfo crudInfo = buildCrudInfoFromChanges(changes);
        
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(clientEmail)
                .setCrudType(CRUD_TYPE_UPDATE)
                .setCrudInfo(crudInfo)
                .build();
        
        kafkaProducer.produceMessage(clientId, c2c, true);
        logger.info("Successfully sent C2C message for client update");
    }
    
    private CRUDInfo buildCrudInfoFromChanges(Map<String, Map.Entry<String, String>> changes) {
        if (changes.isEmpty()) {
            return CRUDInfo.newBuilder().build();
        }
        
        String attributeNames = changes.keySet().stream().collect(Collectors.joining(","));
        String beforeValues = changes.values().stream().map(Map.Entry::getKey).collect(Collectors.joining(","));
        String afterValues = changes.values().stream().map(Map.Entry::getValue).collect(Collectors.joining(","));
        
        return CRUDInfo.newBuilder()
                .setAttribute(attributeNames)
                .setBeforeValue(beforeValues)
                .setAfterValue(afterValues)
                .build();
    }
    
    private void sendClientDeleteKafkaMessage(String clientId, String clientEmail) {
        logger.info("Sending C2C message for client deletion - clientId: {}", clientId);
        
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(clientEmail)
                .setCrudType(CRUD_TYPE_DELETE)
                .build();
        
        kafkaProducer.produceMessage(clientId, c2c, true);
        logger.info("Successfully sent C2C message for client deletion");
    }
    
    private Client validateClientOperation(String clientId, String operation) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (OPERATION_DELETE.equals(operation)) {
            checkForActiveAccounts(clientId);
        }
        
        return client;
    }
    
    private void checkForActiveAccounts(String clientId) {
        List<Account> accounts = accountService.getAccountsByClientId(clientId);
        boolean hasActiveAccounts = accounts.stream()
                .anyMatch(account -> account.getAccountStatus() == AccountStatus.ACTIVE);

        if (hasActiveAccounts) {
            throw new VerificationException("Cannot delete client with active accounts");
        }
    }
}
