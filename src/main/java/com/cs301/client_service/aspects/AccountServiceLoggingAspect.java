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
                // Logged successful retrieval of accounts
            } else {
                // No accounts found for client
            }
        } catch (Exception e) {
            logger.error("Error logging accounts retrieval by client ID", e);
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
