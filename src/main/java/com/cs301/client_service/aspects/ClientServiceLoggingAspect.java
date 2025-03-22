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
        logCreateOperation(result, result.getClientId());
    }

    /**
     * Log after client retrieval
     */
    @AfterReturning(pointcut = "clientRetrieval()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientRetrieval(JoinPoint joinPoint, Client result) {
        logReadOperation(result, result.getClientId());
    }

    /**
     * Log after client update
     */
    @AfterReturning(pointcut = "clientUpdate()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientUpdate(JoinPoint joinPoint, Client result) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        Client newClient = (Client) args[1];
        
        // Get the existing client directly from the database
        Client oldClient = clientRepository.findById(clientId).orElse(null);
        
        if (oldClient != null) {
            // Compare old and new client to detect all changes
            Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(oldClient, newClient);
            
            if (!changes.isEmpty()) {
                // Create a consolidated string of all attribute names
                StringBuilder attributeNames = new StringBuilder();
                // Create consolidated strings for before and after values
                StringBuilder beforeValues = new StringBuilder();
                StringBuilder afterValues = new StringBuilder();
                
                boolean first = true;
                for (Map.Entry<String, Map.Entry<String, String>> change : changes.entrySet()) {
                    if (!first) {
                        attributeNames.append(", ");
                        beforeValues.append(", ");
                        afterValues.append(", ");
                    }
                    
                    String attrName = change.getKey();
                    String beforeValue = change.getValue().getKey();
                    String afterValue = change.getValue().getValue();
                    
                    attributeNames.append(attrName);
                    beforeValues.append(attrName).append(": ").append(beforeValue);
                    afterValues.append(attrName).append(": ").append(afterValue);
                    
                    first = false;
                }
                
                Log log = createLogEntry(
                    clientId,
                    newClient,
                    Log.CrudType.UPDATE,
                    attributeNames.toString(),
                    beforeValues.toString(),
                    afterValues.toString()
                );
                
                logRepository.save(log);
                logger.debug("Logged successful update for entity with client ID: {}", clientId);
            } else {
                logger.debug("No changes detected for client update");
            }
        }
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
            // Create a more detailed log entry with client information
            Log log = createLogEntry(
                clientId,
                null,
                Log.CrudType.DELETE,
                null,
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
