package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.KafkaLoggingAspect;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.protobuf.C2C;
import com.cs301.client_service.protobuf.CRUDInfo;
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
 * Aspect for logging account service operations to Kafka.
 */
@Aspect
@Component
public class AccountKafkaLoggingAspect extends KafkaLoggingAspect {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Pointcut for account creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.createAccount(..))")
    public void accountCreation() {}

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
     * Log after account creation to Kafka
     */
    @AfterReturning(pointcut = "accountCreation()", returning = "result")
    public void logAfterAccountCreation(JoinPoint joinPoint, Account result) {
        String clientId = result.getClient().getClientId();
        String email = result.getClient().getEmailAddress();
        
        // For account creation, we use a simplified attribute/value approach
        // since the protobuf schema is primarily designed for client events
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("Account Creation")
                .setBeforeValue("")
                .setAfterValue("Account ID: " + result.getAccountId())
                .build();
        
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(email)
                .setCrudType("CREATE")
                .setCrudInfo(crudInfo)
                .build();
        
        // Pass true to indicate successful operation
        kafkaProducer.produceMessage(result.getAccountId(), c2c, true);
    }

    /**
     * Log after account update to Kafka
     */
    @AfterReturning(pointcut = "accountUpdate()", returning = "result")
    public void logAfterAccountUpdate(JoinPoint joinPoint, Account result) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        Account newAccount = (Account) args[1];
        
        // Get the existing account directly from the database
        Account oldAccount = accountRepository.findById(accountId).orElse(null);
        
        if (oldAccount != null) {
            String clientId = result.getClient().getClientId();
            String email = result.getClient().getEmailAddress();
            
            // Compare old and new account to detect all changes
            Map<String, Map.Entry<String, String>> changes = LoggingUtils.compareEntities(oldAccount, newAccount);
            logUpdateOperationToKafka(oldAccount, newAccount, clientId, email, changes);
        }
    }

    /**
     * Log after account deletion to Kafka
     */
    @AfterReturning("accountDeletion()")
    public void logAfterAccountDeletion(JoinPoint joinPoint) {
        Object[] args = getArgs(joinPoint);
        String accountId = (String) args[0];
        
        // For account deletion, we use a simplified approach since the account is already deleted
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("Account Deletion")
                .setBeforeValue("Account ID: " + accountId)
                .setAfterValue("")
                .build();
        
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId("ACCOUNT_DELETED") // We don't have client ID at this point
                .setClientEmail("") // We don't have client email at this point
                .setCrudType("DELETE")
                .setCrudInfo(crudInfo)
                .build();
        
        // Pass true to indicate successful operation
        kafkaProducer.produceMessage(accountId, c2c, true);
    }
    
    /**
     * Get attribute names for an account entity
     */
    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Account) {
            return "accountId,clientId,accountType,accountStatus,openingDate,initialDeposit,currency,branchId";
        }
        return "";
    }
    
    /**
     * Get entity values as a string
     */
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Account) {
            Account account = (Account) entity;
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                account.getAccountId(),
                account.getClient().getClientId(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpeningDate(),
                account.getInitialDeposit(),
                account.getCurrency(),
                account.getBranchId()
            );
        }
        return "";
    }
}
