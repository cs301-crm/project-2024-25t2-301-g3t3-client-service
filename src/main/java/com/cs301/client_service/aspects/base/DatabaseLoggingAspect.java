package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Base class for database logging aspects.
 * Provides common functionality for logging operations to the database.
 */
@Order(1) // Database logging should happen first
public abstract class DatabaseLoggingAspect extends BaseLoggingAspect {
    
    @Autowired
    protected LogRepository logRepository;
    
    /**
     * Log a create operation
     * Only logs if the API call was successful
     */
    protected void logCreateOperation(Object entity, String clientId) {
        try {
            // Check if entity is not null, indicating a successful operation
            if (entity != null) {
                Log log = createLogEntry(
                    clientId,
                    entity,
                    Log.CrudType.CREATE,
                    null,
                    "",
                    LoggingUtils.convertToString(entity)
                );

                logRepository.save(log);
                logger.debug("Logged successful creation for entity with client ID: {}", clientId);
            } else {
                logger.debug("Skipping database logging due to unsuccessful API call");
            }
        } catch (Exception e) {
            logException("create", e);
        }
    }
    
    /**
     * Log a read operation
     * Only logs if the API call was successful
     */
    protected void logReadOperation(Object entity, String clientId) {
        try {
            // Check if entity is not null, indicating a successful operation
            if (entity != null) {
                Log log = createLogEntry(
                    clientId,
                    entity,
                    Log.CrudType.READ,
                    null,
                    LoggingUtils.convertToString(entity),
                    LoggingUtils.convertToString(entity)
                );
                
                logRepository.save(log);
                logger.debug("Logged successful read for entity with client ID: {}", clientId);
            } else {
                logger.debug("Skipping database logging due to unsuccessful API call");
            }
        } catch (Exception e) {
            logException("read", e);
        }
    }
    
    /**
     * Log an update operation
     * Only logs if the API call was successful
     */
    protected void logUpdateOperation(Object oldEntity, Object newEntity, String clientId, Map<String, Map.Entry<String, String>> changes) {
        try {
            // Check if newEntity is not null, indicating a successful operation
            if (newEntity != null && changes != null && !changes.isEmpty()) {
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
                    newEntity,
                    Log.CrudType.UPDATE,
                    attributeNames.toString(),
                    beforeValues.toString(),
                    afterValues.toString()
                );
                
                logRepository.save(log);
                logger.debug("Logged successful update for entity with client ID: {}", clientId);
            } else {
                logger.debug("Skipping database logging due to unsuccessful API call or no changes");
            }
        } catch (Exception e) {
            logException("update", e);
        }
    }
    
    /**
     * Log a delete operation
     * Only logs if the API call was successful and IDs are valid
     */
    protected void logDeleteOperation(String entityId, String clientId, String entityType) {
        try {
            // Check if entityId and clientId are not null or empty, indicating a successful operation
            if (entityId != null && !entityId.isEmpty() && clientId != null && !clientId.isEmpty()) {
                Log log = createLogEntry(
                    clientId,
                    null,
                    Log.CrudType.DELETE,
                    null,
                    entityType + " with ID: " + entityId,
                    ""
                );
                
                logRepository.save(log);
                logger.debug("Logged successful deletion for {} with ID: {}", entityType, entityId);
            } else {
                logger.debug("Skipping database logging due to unsuccessful API call or invalid IDs");
            }
        } catch (Exception e) {
            logException("delete", e);
        }
    }
    
    /**
     * Extract client ID from a Client entity
     */
    @Override
    protected String extractClientId(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getClientId();
        } else if (entity instanceof Account) {
            return ((Account) entity).getClient().getClientId();
        } else {
            return "UNKNOWN";
        }
    }
}
