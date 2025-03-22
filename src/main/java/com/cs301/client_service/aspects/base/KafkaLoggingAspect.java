package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.protobuf.C2C;
import com.cs301.client_service.protobuf.CRUDInfo;
import com.cs301.client_service.utils.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Base class for Kafka logging aspects.
 * Provides common functionality for logging operations to Kafka.
 */
@Order(2) // Kafka logging should happen after database logging
public abstract class KafkaLoggingAspect extends BaseLoggingAspect {
    
    @Autowired
    protected KafkaProducer kafkaProducer;
    
    /**
     * Log a create operation to Kafka
     */
    protected void logCreateOperationToKafka(Object entity, String clientId, String email) {
        try {
            logger.debug("Publishing creation event to Kafka for entity with client ID: {}", clientId);
            
            CRUDInfo crudInfo = CRUDInfo.newBuilder()
                    .setAttribute(getAttributeNames(entity))
                    .setBeforeValue("")
                    .setAfterValue(getEntityValues(entity))
                    .build();
            
            C2C c2c = C2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType("CREATE")
                    .setCrudInfo(crudInfo)
                    .build();
            
            // Pass true to indicate successful operation
            kafkaProducer.produceMessage(getMessageKey(entity), c2c, true);
        } catch (Exception e) {
            logException("Kafka create", e);
        }
    }
    
    /**
     * Log an update operation to Kafka
     */
    protected void logUpdateOperationToKafka(Object oldEntity, Object newEntity, String clientId, String email, 
                                           Map<String, Map.Entry<String, String>> changes) {
        try {
            logger.debug("Publishing update event to Kafka for entity with client ID: {}", clientId);
            
            if (changes != null && !changes.isEmpty()) {
                // Create a consolidated string of all attribute names
                StringBuilder attributeNames = new StringBuilder();
                // Create consolidated strings for before and after values
                StringBuilder beforeValues = new StringBuilder();
                StringBuilder afterValues = new StringBuilder();
                
                boolean first = true;
                for (Map.Entry<String, Map.Entry<String, String>> change : changes.entrySet()) {
                    if (!first) {
                        attributeNames.append(",");
                        beforeValues.append(",");
                        afterValues.append(",");
                    }
                    
                    String attrName = change.getKey();
                    String beforeValue = change.getValue().getKey();
                    String afterValue = change.getValue().getValue();
                    
                    attributeNames.append(attrName);
                    beforeValues.append(beforeValue);
                    afterValues.append(afterValue);
                    
                    first = false;
                }
                
                CRUDInfo crudInfo = CRUDInfo.newBuilder()
                        .setAttribute(attributeNames.toString())
                        .setBeforeValue(beforeValues.toString())
                        .setAfterValue(afterValues.toString())
                        .build();
                
                C2C c2c = C2C.newBuilder()
                        .setAgentId(LoggingUtils.getCurrentAgentId())
                        .setClientId(clientId)
                        .setClientEmail(email)
                        .setCrudType("UPDATE")
                        .setCrudInfo(crudInfo)
                        .build();
                
                // Pass true to indicate successful operation
                kafkaProducer.produceMessage(getMessageKey(newEntity), c2c, true);
            }
        } catch (Exception e) {
            logException("Kafka update", e);
        }
    }
    
    /**
     * Log a delete operation to Kafka
     */
    protected void logDeleteOperationToKafka(Object entity, String clientId, String email) {
        try {
            logger.debug("Publishing deletion event to Kafka for entity with client ID: {}", clientId);
            
            CRUDInfo crudInfo = CRUDInfo.newBuilder()
                    .setAttribute(getAttributeNames(entity))
                    .setBeforeValue(getEntityValues(entity))
                    .setAfterValue("")
                    .build();
            
            C2C c2c = C2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType("DELETE")
                    .setCrudInfo(crudInfo)
                    .build();
            
            // Pass true to indicate successful operation
            kafkaProducer.produceMessage(getMessageKey(entity), c2c, true);
        } catch (Exception e) {
            logException("Kafka delete", e);
        }
    }
    
    /**
     * Log a verification operation to Kafka
     */
    protected void logVerificationOperationToKafka(String clientId, String email) {
        try {
            logger.debug("Publishing verification event to Kafka for client ID: {}", clientId);
            
            CRUDInfo crudInfo = CRUDInfo.newBuilder()
                    .setAttribute("verificationStatus")
                    .setBeforeValue("PENDING")
                    .setAfterValue("VERIFIED")
                    .build();
            
            C2C c2c = C2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType("UPDATE")
                    .setCrudInfo(crudInfo)
                    .build();
            
            // Pass true to indicate successful operation
            kafkaProducer.produceMessage(clientId, c2c, true);
        } catch (Exception e) {
            logException("Kafka verification", e);
        }
    }
    
    /**
     * Get attribute names for an entity
     */
    protected abstract String getAttributeNames(Object entity);
    
    /**
     * Get entity values as a string
     */
    protected abstract String getEntityValues(Object entity);
    
    /**
     * Get message key for Kafka
     */
    protected String getMessageKey(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getClientId();
        } else if (entity instanceof Account) {
            return ((Account) entity).getAccountId();
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Extract client ID from an entity
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
    
    /**
     * Extract email from an entity
     */
    protected String extractEmail(Object entity) {
        if (entity instanceof Client) {
            return ((Client) entity).getEmailAddress();
        } else if (entity instanceof Account) {
            return ((Account) entity).getClient().getEmailAddress();
        } else {
            return "";
        }
    }
}
