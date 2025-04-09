package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.mappers.LogMapper;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-logs")
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
    public ResponseEntity<List<LogDTO>> getAllLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<Log> logsPage = logRepository.findAll(pageable);
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }

    @GetMapping("/client")
    public ResponseEntity<List<LogDTO>> getLogsByClientId(
            @RequestParam String clientId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<Log> logsPage;
        
        if (searchQuery != null && !searchQuery.isEmpty()) {
            logsPage = logRepository.findByClientIdWithSearch(clientId, searchQuery, pageable);
        } else {
            logsPage = logRepository.findByClientId(clientId, pageable);
        }
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }

    @GetMapping("/type/{crudType}")
    public ResponseEntity<List<LogDTO>> getLogsByCrudType(
            @PathVariable Log.CrudType crudType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<Log> logsPage = logRepository.findByCrudType(crudType, pageable);
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }
    
    @GetMapping("/agent")
    public ResponseEntity<List<LogDTO>> getLogsByAgentId(
            @RequestParam String agentId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<Log> logsPage;
        
        if (searchQuery != null && !searchQuery.isEmpty()) {
            logsPage = logRepository.findByAgentIdWithSearch(agentId, searchQuery, pageable);
        } else {
            logsPage = logRepository.findByAgentId(agentId, pageable);
        }
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }
}
