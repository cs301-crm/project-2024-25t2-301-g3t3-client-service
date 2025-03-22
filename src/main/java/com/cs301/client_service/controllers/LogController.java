package com.cs301.client_service.controllers;

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

    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        return ResponseEntity.ok(logRepository.findAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Log>> getLogsByClientId(@PathVariable String clientId) {
        return ResponseEntity.ok(logRepository.findByClientId(clientId));
    }

    @GetMapping("/type/{crudType}")
    public ResponseEntity<List<Log>> getLogsByCrudType(@PathVariable Log.CrudType crudType) {
        return ResponseEntity.ok(logRepository.findByCrudType(crudType));
    }
}
