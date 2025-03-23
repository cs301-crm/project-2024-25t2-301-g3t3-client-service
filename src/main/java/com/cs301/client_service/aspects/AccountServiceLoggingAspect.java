package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.DatabaseLoggingAspect;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.AccountRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aspect for logging account service operations to the database.
 */
@Aspect
@Component
public class AccountServiceLoggingAspect extends DatabaseLoggingAspect {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    protected CrudRepository<Account, String> getRepository() {
        return accountRepository;
    }

    @Override
    protected String getEntityId(Object entity) {
        if (entity instanceof Account) {
            return ((Account) entity).getAccountId();
        }
        return null;
    }

    @Override
    protected String getClientId(Object entity) {
        if (entity instanceof Account && ((Account) entity).getClient() != null) {
            return ((Account) entity).getClient().getClientId();
        }
        return null;
    }

    @Override
    protected String getEntityType() {
        return "Account";
    }

    @Override
    protected Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity) {
        Map<String, Map.Entry<String, String>> changes = new HashMap<>();
        
        if (oldEntity instanceof Account && newEntity instanceof Account) {
            Account oldAccount = (Account) oldEntity;
            Account newAccount = (Account) newEntity;
            
            // Compare accountStatus
            if (!oldAccount.getAccountStatus().equals(newAccount.getAccountStatus())) {
                changes.put("accountStatus", new AbstractMap.SimpleEntry<>(
                    oldAccount.getAccountStatus().toString(), newAccount.getAccountStatus().toString()));
            }
            
            // Compare accountType
            if (!oldAccount.getAccountType().equals(newAccount.getAccountType())) {
                changes.put("accountType", new AbstractMap.SimpleEntry<>(
                    oldAccount.getAccountType().toString(), newAccount.getAccountType().toString()));
            }
            
            // Compare currency
            if (!oldAccount.getCurrency().equals(newAccount.getCurrency())) {
                changes.put("currency", new AbstractMap.SimpleEntry<>(
                    oldAccount.getCurrency(), newAccount.getCurrency()));
            }
            
            // Compare branchId
            if (!oldAccount.getBranchId().equals(newAccount.getBranchId())) {
                changes.put("branchId", new AbstractMap.SimpleEntry<>(
                    oldAccount.getBranchId(), newAccount.getBranchId()));
            }
            
            // Add more field comparisons as needed
        }
        
        return changes;
    }

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
     * Pointcut for accounts retrieval by client ID
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.getAccountsByClientId(..))")
    public void accountsRetrievalByClientId() {}

    /**
     * Pointcut for account update
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.updateAccount(..))")
    public void accountUpdate() {}

    /**
     * Pointcut for account deletion
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.deleteAccount(..)) && args(accountId)")
    public void accountDeletion(String accountId) {}

    /**
     * Log after account creation
     */
    @AfterReturning(pointcut = "accountCreation()", returning = "result")
    public void logAfterAccountCreation(JoinPoint joinPoint, Account result) {
        logAfterCreation(joinPoint, result);
    }

    /**
     * Log after account retrieval
     */
    @AfterReturning(pointcut = "accountRetrieval()", returning = "result")
    public void logAfterAccountRetrieval(JoinPoint joinPoint, Account result) {
        logAfterRetrieval(joinPoint, result);
    }
    
    /**
     * Log after accounts retrieval by client ID
     */
    @AfterReturning(pointcut = "accountsRetrievalByClientId()", returning = "result")
    public void logAfterAccountsRetrievalByClientId(JoinPoint joinPoint, List<Account> result) {
        try {
            Object[] args = getArgs(joinPoint);
            String clientId = (String) args[0];
            
            if (result != null && !result.isEmpty()) {
                // Create a log entry
                Log log = createLogEntry(
                    clientId,
                    null,
                    Log.CrudType.READ,
                    clientId, // Store clientId in attributeName
                    "",
                    ""
                );
                
                logRepository.save(log);
                logger.debug("Logged successful retrieval of {} accounts for client: {}", 
                            result.size(), clientId);
            } else {
                logger.debug("No accounts found for client: {}", clientId);
            }
        } catch (Exception e) {
            logger.error("Error logging accounts retrieval by client ID", e);
        }
    }
    
    /**
     * Log after account update
     * 
     * Format:
     * - attributeName: pipe-separated attribute names (e.g., "accountStatus|currency")
     * - beforeValue: pipe-separated values (e.g., "ACTIVE|SGD")
     * - afterValue: pipe-separated values (e.g., "INACTIVE|USD")
     */
    @AfterReturning(pointcut = "accountUpdate()", returning = "result")
    public void logAfterAccountUpdate(JoinPoint joinPoint, Account result) {
        try {
            Object[] args = getArgs(joinPoint);
            String accountId = (String) args[0];
            Account newAccount = result; // Use the result which is the updated account
            
            // Get the existing account to compare with
            Account oldAccount = accountRepository.findById(accountId).orElse(null);
            
            if (oldAccount == null) {
                logger.warn("Account not found for update with ID: {}", accountId);
                return;
            }
            
            String clientId = oldAccount.getClient().getClientId();
            
            // Compare old and new accounts to determine what changed
            Map<String, Map.Entry<String, String>> changes = compareEntities(oldAccount, newAccount);
            
            // Only create a log if there were changes
            if (!changes.isEmpty()) {
                // Create consolidated strings for attribute names, before values, and after values
                StringBuilder attributeNames = new StringBuilder();
                StringBuilder beforeValues = new StringBuilder();
                StringBuilder afterValues = new StringBuilder();
                
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
                
                // Create and save the log entry
                Log log = createLogEntry(
                    clientId,
                    newAccount,
                    Log.CrudType.UPDATE,
                    attributeNames.toString(),
                    beforeValues.toString(),
                    afterValues.toString()
                );
                
                logRepository.save(log);
                logger.debug("Logged update for account with ID: {} for client: {}", 
                            accountId, clientId);
            }
        } catch (Exception e) {
            logger.error("Error logging account update", e);
        }
    }

    /**
     * Log before account deletion
     */
    @Before("accountDeletion(accountId)")
    public void logBeforeAccountDeletion(String accountId) {
        logBeforeDeletion(accountId);
    }
}
