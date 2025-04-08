package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogMapper {
    @Autowired
    private ClientRepository clientRepository;

    /**
     * Convert a Log entity to a LogDTO with a simplified message
     */
    public LogDTO toDTO(Log log) {
        // Generate client name from clientId
        String clientId = log.getClientId() != null ? log.getClientId() : "";
        final StringBuilder clientNameBuilder = new StringBuilder();
        
        if (!clientId.isEmpty()) {
            // Use findById() which returns Optional and handle the case where client might not exist
            clientRepository.findById(clientId).ifPresent(client -> 
                clientNameBuilder.append(client.getFirstName()).append(" ").append(client.getLastName())
            );
        }

        // Generate message based on log type and attributes
        String message = generateMessage(log);
            
        return LogDTO.builder()
                .id(log.getId())
                .agentId(log.getAgentId())
                .clientId(log.getClientId())
                .clientName(clientNameBuilder.toString())
                .crudType(log.getCrudType() != null ? log.getCrudType().name() : null)
                .dateTime(log.getDateTime() != null ? log.getDateTime().toString() : null)
                .attributeName(log.getAttributeName())
                .beforeValue(log.getBeforeValue())
                .afterValue(log.getAfterValue())
                .message(message)
                .build();
    }

    /**
     * Generate a human-readable message based on the log type and attributes
     */
    private String generateMessage(Log log) {
        if (log.getCrudType() == null) {
            return "Unknown operation";
        }

        String operation = log.getCrudType().name().toLowerCase();
        String entityType = determineEntityType(log.getAttributeName());
        
        switch (log.getCrudType()) {
            case CREATE:
                return String.format("Created %s for %s", entityType, log.getClientId());
            case READ:
                return String.format("Retrieved %s for %s", entityType, log.getClientId());
            case UPDATE:
                return String.format("Updated %s for %s", entityType, log.getClientId());
            case DELETE:
                return String.format("Deleted %s for %s", entityType, log.getClientId());
            default:
                return String.format("%s operation on %s for %s", operation, entityType, log.getClientId());
        }
    }

    /**
     * Determine the type of entity based on the attribute name
     */
    private String determineEntityType(String attributeName) {
        if (attributeName == null) {
            return "entity";
        }
        
        // If the attribute name is a UUID, it's likely an account
        if (attributeName.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            return "account";
        }
        
        // If the attribute name contains specific fields, it's likely a profile
        if (attributeName.contains("firstName") || attributeName.contains("lastName") || 
            attributeName.contains("email") || attributeName.contains("phone")) {
            return "profile";
        }
        
        return "entity";
    }

    /**
     * Convert a list of Log entities to a list of LogDTOs
     */
    public List<LogDTO> toDTOList(List<Log> logs) {
        return logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
