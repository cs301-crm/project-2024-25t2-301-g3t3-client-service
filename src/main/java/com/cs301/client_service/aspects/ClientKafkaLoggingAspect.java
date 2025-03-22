package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.KafkaLoggingAspect;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Aspect for logging client service operations to Kafka.
 */
@Aspect
@Component
public class ClientKafkaLoggingAspect extends KafkaLoggingAspect {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Pointcut for client creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.createClient(..))")
    public void clientCreation() {}

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
     * Log after client creation to Kafka
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        logCreateOperationToKafka(result, result.getClientId(), result.getEmailAddress());
    }

    /**
     * Log after client update to Kafka
     */
    @AfterReturning(pointcut = "clientUpdate()", returning = "result")
    public void logAfterClientUpdate(JoinPoint joinPoint, Client result) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        Client newClient = (Client) args[1];
        
        // Get the existing client directly from the database
        Client oldClient = clientRepository.findById(clientId).orElse(null);
        
        if (oldClient != null) {
            // Compare old and new client to detect all changes
            Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(oldClient, newClient);
            logUpdateOperationToKafka(oldClient, newClient, clientId, oldClient.getEmailAddress(), changes);
        }
    }

    /**
     * Log after client deletion to Kafka
     */
    @AfterReturning("clientDeletion()")
    public void logAfterClientDeletion(JoinPoint joinPoint) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        
        // Get the client before deletion
        Client client = clientRepository.findById(clientId).orElse(null);
        
        if (client != null) {
            logDeleteOperationToKafka(client, clientId, client.getEmailAddress());
        }
    }
    
    /**
     * Log after client verification to Kafka
     */
    @AfterReturning(pointcut = "clientVerification()", returning = "result")
    public void logAfterClientVerification(JoinPoint joinPoint, Client result) {
        Object[] args = getArgs(joinPoint);
        String clientId = (String) args[0];
        
        logVerificationOperationToKafka(clientId, result.getEmailAddress());
    }
    
    /**
     * Get attribute names for a client entity
     */
    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Client) {
            return LoggingUtils.getClientAttributeNames();
        }
        return "";
    }
    
    /**
     * Get entity values as a string
     */
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Client) {
            return LoggingUtils.convertClientToCommaSeparatedValues((Client) entity);
        }
        return "";
    }
}
