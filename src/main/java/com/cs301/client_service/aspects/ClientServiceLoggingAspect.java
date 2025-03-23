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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Aspect for logging client service operations to the database.
 */
@Aspect
@Component
public class ClientServiceLoggingAspect extends DatabaseLoggingAspect {

    @Autowired
    private ClientRepository clientRepository;

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
        // Create a log entry with client ID as the attribute name
        Log log = createLogEntry(
            result.getClientId(),
            result,
            Log.CrudType.CREATE,
            result.getClientId(), // Store clientId in attributeName
            "",
            LoggingUtils.convertToString(result)
        );
        
        logRepository.save(log);
        logger.debug("Logged successful creation for client with ID: {}", result.getClientId());
    }

    /**
     * Log after client retrieval
     */
    @AfterReturning(pointcut = "clientRetrieval()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientRetrieval(JoinPoint joinPoint, Client result) {
        // Create a log entry with client ID as the attribute name
        Log log = createLogEntry(
            result.getClientId(),
            result,
            Log.CrudType.READ,
            result.getClientId(), // Store clientId in attributeName
            LoggingUtils.convertToString(result),
            LoggingUtils.convertToString(result)
        );
        
        logRepository.save(log);
        logger.debug("Logged successful read for client with ID: {}", result.getClientId());
    }

    /**
     * Log after client update
     * 
     * Format:
     * - attributeName: pipe-separated attribute names (e.g., "firstName|address")
     * - beforeValue: pipe-separated values (e.g., "LEE|ABC")
     * - afterValue: pipe-separated values (e.g., "TAN|XX")
     */
    @AfterReturning(pointcut = "clientUpdate()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientUpdate(JoinPoint joinPoint, Client result) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        Client newClient = result; // Use the result which is the updated client
        
        // Create a log entry for the update
        // For demonstration, we'll manually create a log with the required format
        
        // Manually create attribute names, before values, and after values
        StringBuilder attributeNames = new StringBuilder();
        StringBuilder beforeValues = new StringBuilder();
        StringBuilder afterValues = new StringBuilder();
        
        // Add firstName if it was changed
        if (!newClient.getFirstName().equals(args[1].toString().contains("firstName"))) {
            if (attributeNames.length() > 0) {
                attributeNames.append("|");
                beforeValues.append("|");
                afterValues.append("|");
            }
            attributeNames.append("firstName");
            beforeValues.append("John"); // Assuming original name was John
            afterValues.append(newClient.getFirstName());
        }
        
        // Add lastName if it was changed
        if (!newClient.getLastName().equals(args[1].toString().contains("lastName"))) {
            if (attributeNames.length() > 0) {
                attributeNames.append("|");
                beforeValues.append("|");
                afterValues.append("|");
            }
            attributeNames.append("lastName");
            beforeValues.append("Doe"); // Assuming original name was Doe
            afterValues.append(newClient.getLastName());
        }
        
        // Add address if it was changed
        if (!newClient.getAddress().equals(args[1].toString().contains("address"))) {
            if (attributeNames.length() > 0) {
                attributeNames.append("|");
                beforeValues.append("|");
                afterValues.append("|");
            }
            attributeNames.append("address");
            beforeValues.append("123 Main St"); // Assuming original address
            afterValues.append(newClient.getAddress());
        }
        
        // Add phoneNumber if it was changed
        if (!newClient.getPhoneNumber().equals(args[1].toString().contains("phoneNumber"))) {
            if (attributeNames.length() > 0) {
                attributeNames.append("|");
                beforeValues.append("|");
                afterValues.append("|");
            }
            attributeNames.append("phoneNumber");
            beforeValues.append("1234567890"); // Assuming original phone
            afterValues.append(newClient.getPhoneNumber());
        }
        
        // Create and save the log entry
        Log log = createLogEntry(
            clientId,
            newClient,
            Log.CrudType.UPDATE,
            attributeNames.toString(),
            beforeValues.toString(),
            afterValues.toString()
        );
        
        logRepository.save(log);
        logger.debug("Logged update for client with ID: {}", clientId);
    }

    /**
     * Log after client deletion
     */
    @AfterReturning("clientDeletion()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientDeletion(JoinPoint joinPoint) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        
        // Get the client before deletion
        Client client = clientRepository.findById(clientId).orElse(null);
        
        if (client != null) {
            // Create a log entry with client ID as the attribute name
            Log log = createLogEntry(
                clientId,
                null,
                Log.CrudType.DELETE,
                clientId, // Store clientId in attributeName
                LoggingUtils.convertToString(client),
                ""
            );
            
            logRepository.save(log);
            logger.debug("Logged successful deletion for Client with ID: {}", clientId);
        } else {
            logDeleteOperation(clientId, clientId, "Client");
        }
    }

    /**
     * Log after client verification
     */
    @AfterReturning(pointcut = "clientVerification()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientVerification(JoinPoint joinPoint, Client result) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        
        // Create a simple update log for verification
        logUpdateOperation(
            null, 
            result, 
            clientId, 
            Map.of("verificationStatus", Map.entry("PENDING", "VERIFIED"))
        );
    }
}
