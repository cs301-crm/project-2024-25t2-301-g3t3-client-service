package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.mappers.LogMapper;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    @Autowired
    private LogRepository logRepository;
    
    @Autowired
    private LogMapper logMapper;

    @GetMapping
    public ResponseEntity<List<LogDTO>> getAllLogs() {
        List<Log> logs = logRepository.findAll();
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<LogDTO>> getLogsByClientId(@PathVariable String clientId) {
        List<Log> logs = logRepository.findByClientId(clientId);
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }

    @GetMapping("/type/{crudType}")
    public ResponseEntity<List<LogDTO>> getLogsByCrudType(@PathVariable Log.CrudType crudType) {
        List<Log> logs = logRepository.findByCrudType(crudType);
        return ResponseEntity.ok(logMapper.toDTOList(logs));
    }
}
