package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.DatabaseLoggingAspect;
import com.cs301.client_service.models.Account;
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
        logCreateOperation(result, result.getClient().getClientId());
    }

    /**
     * Log after account retrieval
     */
    @AfterReturning(pointcut = "accountRetrieval()", returning = "result")
    public void logAfterAccountRetrieval(JoinPoint joinPoint, Account result) {
        logReadOperation(result, result.getClient().getClientId());
    }

    /**
     * Log after account update
     */
    @AfterReturning(pointcut = "accountUpdate()", returning = "result")
    public void logAfterAccountUpdate(JoinPoint joinPoint, Account result) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        Account newAccount = (Account) args[1];
        
        // Get the existing account directly from the database
        Account oldAccount = accountRepository.findById(accountId).orElse(null);
        
        if (oldAccount != null) {
            // Compare old and new account to detect all changes
            Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(oldAccount, newAccount);
            logUpdateOperation(oldAccount, newAccount, result.getClient().getClientId(), changes);
        }
    }

    /**
     * Log after account deletion
     */
    @AfterReturning("accountDeletion()")
    public void logAfterAccountDeletion(JoinPoint joinPoint) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        
        // For deletion, we need to store information about the deleted account
        // Since the account is already deleted at this point, we can't retrieve it
        // We'll just log the deletion event with the account ID
        logDeleteOperation(accountId, "ACCOUNT_DELETED", "Account");
    }
}
