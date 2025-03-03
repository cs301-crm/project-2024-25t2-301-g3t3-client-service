package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.services.impl.ClientServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private static final String VERIFIED = "verified";

    private final ClientServiceImpl clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientServiceImpl clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        logger.info("ClientController initialized");
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        try {
            logger.debug("Received create client request: {}", clientDTO);
            var clientModel = clientMapper.toModel(clientDTO);
            logger.debug("Mapped to model: {}", clientModel);
            var savedClient = clientService.createClient(clientModel);
            logger.debug("Saved client: {}", savedClient);
            var response = clientMapper.toDto(savedClient);
            logger.debug("Returning response: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating client: ", e);
            throw e;
        }
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable String clientId) {
        var client = clientService.getClient(clientId);
        return ResponseEntity.ok(clientMapper.toDto(client));
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
        return ResponseEntity.ok(clientMapper.toDto(updatedClient));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * TODO: Verify a client's identity using NRIC??? Fix later
     */
    @PostMapping("/{clientId}/verify")
    public ResponseEntity<Map<String, Boolean>> verifyClient(
            @PathVariable String clientId,
            @RequestBody Map<String, String> payload) {

        String nric = payload.get("nric");
        if (nric == null || nric.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(VERIFIED, false));
        }

        try {
            clientService.verifyClient(clientId, nric);
            return ResponseEntity.ok(Map.of(VERIFIED, true));
        } catch (ClientNotFoundException | VerificationException e) {
            logger.warn("Verification failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(VERIFIED, false));
        }
    }
}
