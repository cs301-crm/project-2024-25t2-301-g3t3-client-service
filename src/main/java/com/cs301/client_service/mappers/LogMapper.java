package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.models.Log;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogMapper {

    /**
     * Convert a Log entity to a LogDTO with a simplified message
     */
    public LogDTO toDTO(Log log) {
        return LogDTO.builder()
                .id(log.getId())
                .message(generateSimplifiedMessage(log))
                .clientId(log.getClientId())
                .agentId(log.getAgentId())
                .dateTime(log.getDateTime())
                .attributeName(log.getAttributeName())
                .beforeValue(log.getBeforeValue())
                .afterValue(log.getAfterValue())
                .crudType(log.getCrudType() != null ? log.getCrudType().name() : null)
                .build();
    }

    /**
     * Convert a list of Log entities to a list of LogDTOs
     */
    public List<LogDTO> toDTOList(List<Log> logs) {
        return logs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generate a simplified message based on the log details
     */
    private String generateSimplifiedMessage(Log log) {
        // Determine if this is an account-related log or a client-related log
        boolean isAccountLog = false;
        
        // Check if the beforeValue or afterValue contains "accountId" or "accountType"
        if ((log.getBeforeValue() != null && (log.getBeforeValue().contains("accountId") || 
                                             log.getBeforeValue().contains("accountType"))) ||
            (log.getAfterValue() != null && (log.getAfterValue().contains("accountId") || 
                                           log.getAfterValue().contains("accountType")))) {
            isAccountLog = true;
        }
        
        String entityType = isAccountLog ? "account" : "profile";
        
        switch (log.getCrudType()) {
            case CREATE:
                return "Created " + entityType + " for " + log.getClientId();
                
            case READ:
                return "Retrieved " + entityType + " for " + log.getClientId();
                
            case UPDATE:
                // For verification status updates
                if (log.getAttributeName() != null && log.getAttributeName().contains("verificationStatus")) {
                    return "Verified " + entityType + " for " + log.getClientId();
                }
                
                // For regular updates, list the changed attributes
                if (log.getAttributeName() != null && !log.getAttributeName().isEmpty() && 
                    !log.getAttributeName().equals(log.getClientId())) {
                    String attributes = log.getAttributeName().replace("|", ", ");
                    return "Updated " + attributes + " for " + log.getClientId();
                }
                
                return "Updated " + entityType + " for " + log.getClientId();
                
            case DELETE:
                return "Deleted " + entityType + " for " + log.getClientId();
                
            default:
                return "Operation on " + entityType + " for " + log.getClientId();
        }
    }
}
