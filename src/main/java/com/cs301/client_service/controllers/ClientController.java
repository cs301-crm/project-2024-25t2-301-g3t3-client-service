package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.services.ClientService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private static final String VERIFIED = "verified";

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        logger.info("ClientController initialized");
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        var clientModel = clientMapper.toModel(clientDTO);
        var savedClient = clientService.createClient(clientModel);
        var response = clientMapper.toDto(savedClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable String clientId) {
        var client = clientService.getClient(clientId);
        var response = clientMapper.toDto(client);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ClientDTO>> getClientsByAgentId(@PathVariable String agentId) {
        var clients = clientService.getClientsByAgentId(agentId);
        var clientDTOs = clients.stream()
                .map(clientMapper::toDto)
                .toList();
        return ResponseEntity.ok(clientDTOs);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody ClientDTO clientDTO) {
        var clientModel = clientMapper.toModel(clientDTO);
        var updatedClient = clientService.updateClient(clientId, clientModel);
        var response = clientMapper.toDto(updatedClient);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verify a client to activate their profile
     * This changes their status from PENDING to VERIFIED
     */
    @PostMapping("/{clientId}/verify")
    public ResponseEntity<Map<String, Boolean>> verifyClient(@PathVariable String clientId) {
        try {
            clientService.verifyClient(clientId);
            return ResponseEntity.ok(Map.of(VERIFIED, true));
        } catch (ClientNotFoundException ex) {
            // Return a consistent response format for the not found case
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(VERIFIED, false));
        }
    }
}
