package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.DatabaseLoggingAspect;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Aspect for logging account service operations to the database.
 */
@Aspect
@Component
public class AccountServiceLoggingAspect extends DatabaseLoggingAspect {

    @Autowired
    private AccountRepository accountRepository;

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
     * Pointcut for account update
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.updateAccount(..))")
    public void accountUpdate() {}

    /**
     * Pointcut for account deletion
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.deleteAccount(..))")
    public void accountDeletion() {}

    /**
     * Log after account creation
     */
    @AfterReturning(pointcut = "accountCreation()", returning = "result")
    public void logAfterAccountCreation(JoinPoint joinPoint, Account result) {
        String clientId = result.getClient().getClientId();
        
        // Create a log entry with client ID as the attribute name
        Log log = createLogEntry(
            clientId,
            result,
            Log.CrudType.CREATE,
            clientId, // Store clientId in attributeName
            "",
            LoggingUtils.convertToString(result)
        );
        
        logRepository.save(log);
        logger.debug("Logged successful creation for account with ID: {} for client: {}", 
                    result.getAccountId(), clientId);
    }

    /**
     * Log after account retrieval
     */
    @AfterReturning(pointcut = "accountRetrieval()", returning = "result")
    public void logAfterAccountRetrieval(JoinPoint joinPoint, Account result) {
        String clientId = result.getClient().getClientId();
        
        // Create a log entry with client ID as the attribute name
        Log log = createLogEntry(
            clientId,
            result,
            Log.CrudType.READ,
            clientId, // Store clientId in attributeName
            LoggingUtils.convertToString(result),
            LoggingUtils.convertToString(result)
        );
        
        logRepository.save(log);
        logger.debug("Logged successful read for account with ID: {} for client: {}", 
                    result.getAccountId(), clientId);
    }

    /**
     * Log after account update
     * 
     * Format:
     * - attributeName: pipe-separated attribute names (e.g., "firstName|address")
     * - beforeValue: pipe-separated values (e.g., "LEE|ABC")
     * - afterValue: pipe-separated values (e.g., "TAN|XX")
     */
    @AfterReturning(pointcut = "accountUpdate()", returning = "result")
    public void logAfterAccountUpdate(JoinPoint joinPoint, Account result) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        Account newAccount = (Account) args[1];
        String clientId = result.getClient().getClientId();
        
        // Get the existing account directly from the database
        Account oldAccount = accountRepository.findById(accountId).orElse(null);
        
        if (oldAccount != null) {
            // Compare old and new account to detect all changes
            Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(oldAccount, newAccount);
            
            if (!changes.isEmpty()) {
                // Use the standard logUpdateOperation method from the parent class
                // This will format the logs according to the requirements
                logUpdateOperation(oldAccount, newAccount, clientId, changes);
            } else {
                logger.debug("No changes detected for account update");
            }
        }
    }

    /**
     * Log after account deletion
     */
    @AfterReturning("accountDeletion()")
    public void logAfterAccountDeletion(JoinPoint joinPoint) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        
        // Get the account before deletion if possible
        Account account = accountRepository.findById(accountId).orElse(null);
        
        if (account != null) {
            String clientId = account.getClient().getClientId();
            
            // Create a log entry with client ID as the attribute name
            Log log = createLogEntry(
                clientId,
                null,
                Log.CrudType.DELETE,
                clientId, // Store clientId in attributeName
                LoggingUtils.convertToString(account),
                ""
            );
            
            logRepository.save(log);
            logger.debug("Logged successful deletion for Account with ID: {} for client: {}", 
                        accountId, clientId);
        } else {
            // If we can't find the account (already deleted), use a generic log
            logDeleteOperation(accountId, "ACCOUNT_DELETED", "Account");
        }
    }
}
