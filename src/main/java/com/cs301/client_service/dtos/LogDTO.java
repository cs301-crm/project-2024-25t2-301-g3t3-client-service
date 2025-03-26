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
    private String message;
    private String clientId;
    private String agentId;
    private LocalDateTime dateTime;
    private String attributeName;
    private String beforeValue;
    private String afterValue;
    private String crudType;
}
