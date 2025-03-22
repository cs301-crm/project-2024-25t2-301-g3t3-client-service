package com.cs301.client_service.aspects;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private LogRepository logRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private AccountRepository accountRepository;

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
     * Pointcut for account creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.createAccount(..))")
    public void accountCreation() {}

    /**
     * Pointcut for account retrieval
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.getAccount(..))")
    public void accountRetrieval() {}

    /**
     * Pointcut for account deletion
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.deleteAccount(..))")
    public void accountDeletion() {}

    /**
     * Log after client creation
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        Log log = Log.builder()
                .crudType(Log.CrudType.CREATE)
                .beforeValue("")
                .afterValue(LoggingUtils.convertToString(result))
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(result.getClientId())
                .dateTime(LocalDateTime.now())
                .build();
        
        logRepository.save(log);
    }

    /**
     * Log after client retrieval
     */
    @AfterReturning(pointcut = "clientRetrieval()", returning = "result")
    public void logAfterClientRetrieval(JoinPoint joinPoint, Client result) {
        Log log = Log.builder()
                .crudType(Log.CrudType.READ)
                .beforeValue(LoggingUtils.convertToString(result))
                .afterValue(LoggingUtils.convertToString(result))
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(result.getClientId())
                .dateTime(LocalDateTime.now())
                .build();
        
        logRepository.save(log);
    }

    /**
     * Log before client update
     */
    @Before("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.updateClient(..))")
    public void logBeforeClientUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
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
                
                // Create a single log entry with all changes
                Log log = Log.builder()
                        .crudType(Log.CrudType.UPDATE)
                        .attributeName(attributeNames.toString())
                        .beforeValue(beforeValues.toString())
                        .afterValue(afterValues.toString())
                        .agentId(LoggingUtils.getCurrentAgentId())
                        .clientId(clientId)
                        .dateTime(LocalDateTime.now())
                        .build();
                
                logRepository.save(log);
            }
        }
    }

    /**
     * Log before client deletion
     */
    @Before("clientDeletion()")
    public void logBeforeClientDeletion(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String clientId = (String) args[0];
        
        // Get the client before deletion
        Client client = clientRepository.findById(clientId).orElse(null);
        
        if (client != null) {
            Log log = Log.builder()
                    .crudType(Log.CrudType.DELETE)
                    .beforeValue(LoggingUtils.convertToString(client))
                    .afterValue("")
                    .agentId(LoggingUtils.getCurrentAgentId())
                    .clientId(clientId)
                    .dateTime(LocalDateTime.now())
                    .build();
            
            logRepository.save(log);
        }
    }

    /**
     * Log after client verification
     */
    @AfterReturning("clientVerification()")
    public void logAfterClientVerification(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String clientId = (String) args[0];
        String nric = (String) args[1];
        
        Log log = Log.builder()
                .crudType(Log.CrudType.READ)
                .attributeName("verification")
                .beforeValue("")
                .afterValue("NRIC verification attempt: " + nric)
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(clientId)
                .dateTime(LocalDateTime.now())
                .build();
        
        logRepository.save(log);
    }

    /**
     * Log after account creation
     */
    @AfterReturning(pointcut = "accountCreation()", returning = "result")
    public void logAfterAccountCreation(JoinPoint joinPoint, Account result) {
        Log log = Log.builder()
                .crudType(Log.CrudType.CREATE)
                .beforeValue("")
                .afterValue(LoggingUtils.convertToString(result))
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(result.getClient().getClientId())
                .dateTime(LocalDateTime.now())
                .build();
        
        logRepository.save(log);
    }

    /**
     * Log after account retrieval
     */
    @AfterReturning(pointcut = "accountRetrieval()", returning = "result")
    public void logAfterAccountRetrieval(JoinPoint joinPoint, Account result) {
        Log log = Log.builder()
                .crudType(Log.CrudType.READ)
                .beforeValue(LoggingUtils.convertToString(result))
                .afterValue(LoggingUtils.convertToString(result))
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(result.getClient().getClientId())
                .dateTime(LocalDateTime.now())
                .build();
        
        logRepository.save(log);
    }

    /**
     * Log before account deletion
     */
    @Before("accountDeletion()")
    public void logBeforeAccountDeletion(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String accountId = (String) args[0];
        
        // Get the account before deletion
        Account account = accountRepository.findById(accountId).orElse(null);
        
        if (account != null) {
            Log log = Log.builder()
                    .crudType(Log.CrudType.DELETE)
                    .beforeValue(LoggingUtils.convertToString(account))
                    .afterValue("")
                    .agentId(LoggingUtils.getCurrentAgentId())
                    .clientId(account.getClient().getClientId())
                    .dateTime(LocalDateTime.now())
                    .build();
            
            logRepository.save(log);
        }
    }
}
