package com.cs301.client_service.models;

import com.cs301.client_service.utils.LoggingUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrudType crudType;
    
    // For single attribute changes
    private String attributeName;
    
    // For multiple attribute changes in UPDATE operations
    @Column(columnDefinition = "TEXT")
    private String attributesJson;
    
    @Column(columnDefinition = "TEXT")
    private String beforeValue;
    
    @Column(columnDefinition = "TEXT")
    private String afterValue;
    
    private String agentId;
    
    private String clientId;
    
    @Column(nullable = false)
    private LocalDateTime dateTime;
    
    public enum CrudType {
        CREATE, READ, UPDATE, DELETE
    }
    
    // Helper method to set multiple attributes
    public void setAttributes(Map<String, Map.Entry<String, String>> attributes) {
        this.attributesJson = LoggingUtils.convertToString(attributes);
    }
    
    // Helper method to get multiple attributes
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributes() {
        if (attributesJson == null || attributesJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            // Parse as a simple Map<String, Object> first
            Map<String, Object> rawMap = LoggingUtils.convertFromString(attributesJson, HashMap.class);
            
            // Convert the nested structures as needed
            Map<String, Map.Entry<String, String>> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Map) {
                    Map<String, String> nestedMap = (Map<String, String>) value;
                    if (nestedMap.size() == 1) {
                        Map.Entry<String, String> firstEntry = nestedMap.entrySet().iterator().next();
                        result.put(key, Map.entry(firstEntry.getKey(), firstEntry.getValue()));
                    }
                }
            }
            
            return (Map<String, Object>) (Object) result;
        } catch (Exception e) {
            System.err.println("Error parsing attributes JSON: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
