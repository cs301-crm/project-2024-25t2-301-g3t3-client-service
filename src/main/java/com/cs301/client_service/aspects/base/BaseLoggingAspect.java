package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Log;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Base abstract class for all logging aspects.
 * Provides common functionality for logging operations.
 */
public abstract class BaseLoggingAspect {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Create a log entry for a create operation
     */
    protected Log createLogEntry(String clientId, Object entity, Log.CrudType crudType, 
                               String attributeName, String beforeValue, String afterValue) {
        return Log.builder()
                .crudType(crudType)
                .attributeName(attributeName)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(clientId)
                .dateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Extract client ID from an entity
     */
    protected abstract String extractClientId(Object entity);
    
    /**
     * Check if a response is successful (for controller aspects)
     */
    protected boolean isSuccessfulResponse(ResponseEntity<?> response) {
        return response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }
    
    /**
     * Extract method arguments from a join point
     */
    protected Object[] getArgs(JoinPoint joinPoint) {
        return joinPoint.getArgs();
    }
    
    /**
     * Log an exception that occurred during aspect execution
     */
    protected void logException(String operation, Exception e) {
        logger.error("Error during {} logging operation", operation, e);
    }
}
