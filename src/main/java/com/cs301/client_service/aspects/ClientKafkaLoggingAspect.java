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

/**
 * Aspect for logging client service operations to Kafka.
 * Only handles client creation as update and delete operations are handled directly in the service.
 */
@Aspect
@Component
public class ClientKafkaLoggingAspect extends KafkaLoggingAspect {

    @Autowired
    private ClientRepository clientRepository;

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
    protected String getClientEmail(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getEmailAddress();
        }
        return null;
    }

    @Override
    protected String getEntityType() {
        return "Client";
    }

    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Client) {
            return LoggingUtils.getClientAttributeNames();
        }
        return "";
    }
    
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Client) {
            return LoggingUtils.convertClientToCommaSeparatedValues((Client) entity);
        }
        return "";
    }

    /**
     * Pointcut for client creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.createClient(..))")
    public void clientCreation() {}

    /**
     * Log after client creation to Kafka
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        logAfterCreationToKafka(joinPoint, result);
    }
}
