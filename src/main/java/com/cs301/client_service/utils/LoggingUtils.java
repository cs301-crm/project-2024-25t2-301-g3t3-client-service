package com.cs301.client_service.utils;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

@Component
public class LoggingUtils {

    private static ObjectMapper objectMapper;

    @Autowired
    public LoggingUtils(ObjectMapper mapper) {
        objectMapper = mapper;
    }
    
    // Fallback if injection fails
    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            // Configure the mapper for proper date handling
            objectMapper.findAndRegisterModules();
        }
        return objectMapper;
    }

    /**
     * Compares two entities and returns a map of changed properties with their before and after values
     * @param oldEntity The entity before changes
     * @param newEntity The entity after changes
     * @return Map with property name as key and pair of before/after values
     */
    public static Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity) {
        if (oldEntity == null || newEntity == null || !oldEntity.getClass().equals(newEntity.getClass())) {
            throw new IllegalArgumentException("Entities must be non-null and of the same type");
        }

        Map<String, Map.Entry<String, String>> changes = new HashMap<>();
        
        try {
            PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(oldEntity.getClass());
            
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyName = propertyDescriptor.getName();
                
                // Skip class property and collections
                if ("class".equals(propertyName) || "accounts".equals(propertyName)) {
                    continue;
                }
                
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    Object oldValue = readMethod.invoke(oldEntity);
                    Object newValue = readMethod.invoke(newEntity);
                    
                    // Check if values are different
                    if ((oldValue == null && newValue != null) || 
                        (oldValue != null && !oldValue.equals(newValue))) {
                        
                        String oldValueStr = oldValue != null ? oldValue.toString() : "";
                        String newValueStr = newValue != null ? newValue.toString() : "";
                        
                        changes.put(propertyName, Map.entry(oldValueStr, newValueStr));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error comparing entities", e);
        }
        
        return changes;
    }

    /**
     * Converts an entity to a JSON string
     * @param entity The entity to convert
     * @return JSON string representation
     */
    public static String convertToString(Object entity) {
        if (entity == null) {
            return "";
        }
        
        try {
            return getObjectMapper().writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting entity to string", e);
        }
    }
    
    /**
     * Converts a JSON string back to an object
     * @param json The JSON string to convert
     * @param valueType The class of the object to convert to
     * @return The converted object
     */
    public static <T> T convertFromString(String json, Class<T> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            return getObjectMapper().readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting string to object", e);
        }
    }

    /**
     * Extracts client ID from an entity
     * @param entity The entity to extract from
     * @return The client ID
     */
    public static String extractClientId(Object entity) {
        if (entity == null) {
            return null;
        }
        
        if (entity instanceof Client) {
            return ((Client) entity).getClientId();
        } else if (entity instanceof Account) {
            return ((Account) entity).getClient().getClientId();
        }
        
        return null;
    }

    /**
     * Gets the current agent ID from security context
     * @return The agent ID
     */
    public static String getCurrentAgentId() {
        // In a real application, this would extract the agent ID from the security context
        // For now, we'll return a placeholder
        return "system";
    }
    
    /**
     * Converts a Client object to a comma-delimited string of values
     * @param client The client to convert
     * @return Comma-delimited string of values
     */
    public static String convertClientToCommaSeparatedValues(Client client) {
        if (client == null) {
            return "";
        }
        
        StringBuilder values = new StringBuilder();
        values.append(client.getClientId()).append(",")
              .append(client.getFirstName()).append(",")
              .append(client.getLastName()).append(",")
              .append(client.getDateOfBirth()).append(",")
              .append(client.getGender()).append(",")
              .append(client.getEmailAddress()).append(",")
              .append(client.getPhoneNumber()).append(",")
              .append(client.getAddress()).append(",")
              .append(client.getCity()).append(",")
              .append(client.getState()).append(",")
              .append(client.getCountry()).append(",")
              .append(client.getPostalCode()).append(",")
              .append(client.getNric()).append(",")
              .append(client.getAgentId());
        
        return values.toString();
    }
    
    /**
     * Gets a comma-delimited string of client attribute names
     * @return Comma-delimited string of attribute names
     */
    public static String getClientAttributeNames() {
        return "clientId,firstName,lastName,dateOfBirth,gender,emailAddress,phoneNumber,address,city,state,country,postalCode,nric,agentId";
    }
}
