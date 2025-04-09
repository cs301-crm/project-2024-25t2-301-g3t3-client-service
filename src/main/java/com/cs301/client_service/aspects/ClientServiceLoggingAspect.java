package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.DatabaseLoggingAspect;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for logging client service operations to the database.
 */
@Aspect
@Component
public class ClientServiceLoggingAspect extends DatabaseLoggingAspect {

    @Autowired
    private ClientRepository clientRepository;
    
    @Override
    protected CrudRepository<Client, String> getRepository() {
        return clientRepository;
    }

    @Override
    protected String getEntityId(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getClientId();
        }
        return null;
    }

    @Override
    protected String getClientId(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getClientId();
        }
        return null;
    }

    @Override
    protected String getEntityType() {
        return "Client";
    }

    @Override
    protected Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity) {
        // Use the LoggingUtils.compareEntities method to compare all properties of the entities
        Map<String, Map.Entry<String, String>> changes = com.cs301.client_service.utils.LoggingUtils.compareEntities(oldEntity, newEntity);
        
        // Remove clientId from changes since it's not actually changing
        changes.remove("clientId");
        
        return changes;
    }

    /**
     * Pointcut for client creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.createClient(..))")
    public void clientCreation() {}

    /**
     * Pointcut for client retrieval
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.getClient(..))")
    public void clientRetrieval() {}

    /**
     * Pointcut for client update
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.updateClient(..))")
    public void clientUpdate() {}

    /**
     * Pointcut for client deletion
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.deleteClient(..))")
    public void clientDeletion() {}

    /**
     * Pointcut for client verification
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.verifyClient(..))")
    public void clientVerification() {}

    /**
     * Log after client creation
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        // Use the parent class method to log the creation with empty before/after values
        logAfterCreation(joinPoint, result);
    }

    /**
     * Log after client retrieval
     */
    @AfterReturning(pointcut = "clientRetrieval()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientRetrieval(JoinPoint joinPoint, Client result) {
        // Use the parent class method to log the retrieval with empty before/after values
        logAfterRetrieval(joinPoint, result);
    }

    /**
     * Log around client update
     * 
     * Format:
     * - attributeName: pipe-separated attribute names (e.g., "firstName|address")
     * - beforeValue: pipe-separated values (e.g., "LEE|ABC")
     * - afterValue: pipe-separated values (e.g., "TAN|XX")
     */
    @org.aspectj.lang.annotation.Around("clientUpdate()")
    public Object logAroundClientUpdate(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Extract method arguments
            Object[] args = getArgs(joinPoint);
            String clientId = (String) args[0];
            Client newClientData = (Client) args[1];
            
            // Log the update attempt
            logger.info("Attempting to log update for client with ID: {}", clientId);
            
            // Get the client BEFORE the update and create a deep copy to preserve the original state
            Client existingClient = clientRepository.findById(clientId).orElse(null);
            
            if (existingClient == null) {
                logger.warn("Client not found for update with ID: {}", clientId);
                return joinPoint.proceed(); // Just proceed with the method execution
            }
            
            // Create a deep copy of the existing client to compare with after the update
            Client originalClientSnapshot = Client.builder()
                .clientId(existingClient.getClientId())
                .firstName(existingClient.getFirstName())
                .lastName(existingClient.getLastName())
                .dateOfBirth(existingClient.getDateOfBirth())
                .gender(existingClient.getGender())
                .emailAddress(existingClient.getEmailAddress())
                .phoneNumber(existingClient.getPhoneNumber())
                .address(existingClient.getAddress())
                .city(existingClient.getCity())
                .state(existingClient.getState())
                .country(existingClient.getCountry())
                .postalCode(existingClient.getPostalCode())
                .nric(existingClient.getNric())
                .agentId(existingClient.getAgentId())
                .verificationStatus(existingClient.getVerificationStatus())
                .build();
            
            // Debug logging for the original client
            logger.debug("Original client before update: {}", originalClientSnapshot);
            logger.debug("New client data for update: {}", newClientData);
            
            // Proceed with the method execution (this performs the actual update)
            Object result = joinPoint.proceed();
            
            // The result is the updated client
            Client updatedClient = (Client) result;
            
            // Debug logging for updated client
            logger.debug("Updated client after update: {}", updatedClient);
            
            // Compare original and updated clients to determine what changed
            Map<String, Map.Entry<String, String>> changes = compareEntities(originalClientSnapshot, updatedClient);
            
            // Debug logging for changes
            logger.debug("Changes detected for client {}: {}", clientId, changes);
            
            // Create consolidated strings for attribute names, before values, and after values
            StringBuilder attributeNames = new StringBuilder();
            StringBuilder beforeValues = new StringBuilder();
            StringBuilder afterValues = new StringBuilder();
            
            // If no changes detected, log a warning but don't create a log entry
            if (changes.isEmpty()) {
                logger.warn("No changes detected for client update with ID: {}, comparison may have failed", clientId);
                
                // For debugging - explicitly check a few key fields
                if (!originalClientSnapshot.getFirstName().equals(updatedClient.getFirstName())) {
                    logger.info("First name changed but not detected: {} -> {}", 
                        originalClientSnapshot.getFirstName(), updatedClient.getFirstName());
                }
                if (!originalClientSnapshot.getLastName().equals(updatedClient.getLastName())) {
                    logger.info("Last name changed but not detected: {} -> {}", 
                        originalClientSnapshot.getLastName(), updatedClient.getLastName());
                }
                if (!originalClientSnapshot.getEmailAddress().equals(updatedClient.getEmailAddress())) {
                    logger.info("Email changed but not detected: {} -> {}", 
                        originalClientSnapshot.getEmailAddress(), updatedClient.getEmailAddress());
                }
            } else {
                boolean first = true;
                for (Map.Entry<String, Map.Entry<String, String>> change : changes.entrySet()) {
                    if (!first) {
                        attributeNames.append("|");
                        beforeValues.append("|");
                        afterValues.append("|");
                    }
                    
                    String attrName = change.getKey();
                    String beforeValue = change.getValue().getKey();
                    String afterValue = change.getValue().getValue();
                    
                    attributeNames.append(attrName);
                    beforeValues.append(beforeValue);
                    afterValues.append(afterValue);
                    
                    first = false;
                }
                
                // Create and save the log entry - only do this when actual changes are detected
                Log log = createLogEntry(
                    clientId,
                    updatedClient,
                    Log.CrudType.UPDATE,
                    attributeNames.toString(),
                    beforeValues.toString(),
                    afterValues.toString()
                );
                
                try {
                    Log savedLog = logRepository.save(log);
                    logger.info("Successfully logged update for client with ID: {}, log ID: {}", clientId, savedLog.getId());
                } catch (Exception e) {
                    logger.error("Failed to save log for client update: {}", e.getMessage(), e);
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error logging client update: {}", e.getMessage(), e);
            return joinPoint.proceed(); // Still proceed with the method execution even if logging fails
        }
    }

    /**
     * Log after client deletion
     */
    @AfterReturning("clientDeletion()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = getArgs(joinPoint);
            String clientId = (String) args[0];
            
            // Get the client before deletion
            Client client = clientRepository.findById(clientId).orElse(null);
            
            if (client == null) {
                logger.warn("Client not found for deletion with ID: {}", clientId);
            }
            
            // Create a log entry with client ID as the attribute name
            Log log = createLogEntry(
                clientId,
                null,
                Log.CrudType.DELETE,
                clientId, // Store clientId in attributeName
                "",
                ""
            );
            
            logRepository.save(log);
            logger.debug("Logged deletion for Client with ID: {}", clientId);
        } catch (Exception e) {
            logger.error("Error logging client deletion", e);
        }
    }

    /**
     * Log after client verification
     */
    @AfterReturning(pointcut = "clientVerification()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientVerification(JoinPoint joinPoint, Client result) {
        try {
            Object[] args = getArgs(joinPoint);
            String clientId = (String) args[0];
            
            if (result == null) {
                logger.warn("Client not found for verification with ID: {}", clientId);
                return;
            }
            
            // Create a log entry with verificationStatus as the changed attribute
            Log log = createLogEntry(
                clientId,
                result,
                Log.CrudType.UPDATE,
                "verificationStatus", // Store the attribute name
                "PENDING",
                "VERIFIED"
            );
            
            logRepository.save(log);
            logger.debug("Logged verification for client with ID: {}", clientId);
        } catch (Exception e) {
            logger.error("Error logging client verification", e);
        }
    }
}
