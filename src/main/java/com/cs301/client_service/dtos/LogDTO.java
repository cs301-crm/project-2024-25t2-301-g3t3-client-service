package com.cs301.client_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String id;
    private String clientId;
    private String clientName; // Added to match frontend expectations
    private String agentId;
    private String dateTime; // Changed from LocalDateTime to String
    private String attributeName;
    private String beforeValue;
    private String afterValue;
    private String crudType;
    private String message; // Added to match test expectations
}
