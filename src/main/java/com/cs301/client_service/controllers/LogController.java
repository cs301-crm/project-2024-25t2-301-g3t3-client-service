package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.mappers.LogMapper;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    
    private final LogRepository logRepository;
    private final LogMapper logMapper;
    
    public LogController(LogRepository logRepository, LogMapper logMapper) {
        this.logRepository = logRepository;
        this.logMapper = logMapper;
        logger.info("LogController initialized");
    }

    @GetMapping
    public ResponseEntity<List<LogDTO>> getAllLogs() {
        var logs = logRepository.findAll();
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<LogDTO>> getLogsByClientId(@PathVariable String clientId) {
        var logs = logRepository.findByClientId(clientId);
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }

    @GetMapping("/type/{crudType}")
    public ResponseEntity<List<LogDTO>> getLogsByCrudType(@PathVariable Log.CrudType crudType) {
        var logs = logRepository.findByCrudType(crudType);
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<LogDTO>> getLogsByAgentId(@PathVariable String agentId) {
        var logs = logRepository.findByAgentId(agentId);
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }
}
