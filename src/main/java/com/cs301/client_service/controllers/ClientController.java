package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.ErrorResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        try {
            logger.debug("Received create client request");
            var clientModel = clientMapper.toModel(clientDTO);
            logger.debug("Client model mapped successfully");
            var savedClient = clientService.createClient(clientModel);
            logger.debug("Client created successfully");
            var response = clientMapper.toDto(savedClient);
            logger.debug("Response mapped successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating client: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to create client: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(@PathVariable String clientId) {
        try {
            logger.debug("Received get client request");
            var client = clientService.getClient(clientId);
            logger.debug("Client retrieved successfully");
            var response = clientMapper.toDto(client);
            logger.debug("Response mapped successfully");
            return ResponseEntity.ok(response);
        } catch (ClientNotFoundException e) {
            logger.warn("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error retrieving client: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to retrieve client: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<?> getClientsByAgentId(@PathVariable String agentId) {
        try {
            logger.debug("Received get clients by agent id request");
            var clients = clientService.getClientsByAgentId(agentId);
            logger.debug("Retrieved {} clients", clients.size());
            var clientDTOs = clients.stream()
                    .map(clientMapper::toDto)
                    .toList();
            logger.debug("Response mapped successfully with {} clients", clientDTOs.size());
            return ResponseEntity.ok(clientDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving clients for agent: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to retrieve clients for agent: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody ClientDTO clientDTO) {
        try {
            logger.debug("Received update client request");
            var clientModel = clientMapper.toModel(clientDTO);
            logger.debug("Client model mapped successfully");
            var updatedClient = clientService.updateClient(clientId, clientModel);
            logger.debug("Client updated successfully");
            var response = clientMapper.toDto(updatedClient);
            logger.debug("Response mapped successfully");
            return ResponseEntity.ok(response);
        } catch (ClientNotFoundException e) {
            logger.warn("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error updating client: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to update client: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<?> deleteClient(@PathVariable String clientId) {
        try {
            logger.debug("Received delete client request");
            clientService.deleteClient(clientId);
            logger.debug("Client deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (ClientNotFoundException e) {
            logger.warn("Client not found exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (VerificationException e) {
            logger.warn("Verification failed exception occurred: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error deleting client: ", e);
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to delete client: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * TODO: Verify a client's identity using NRIC??? Fix later
     */
    @PostMapping("/{clientId}/verify")
    public ResponseEntity<Map<String, Boolean>> verifyClient(
            @PathVariable String clientId,
            @RequestBody Map<String, String> payload) {
        try {
            logger.debug("Received verify client request");
            String nric = payload.get("nric");
            if (nric == null || nric.isBlank()) {
                logger.warn("NRIC is null or blank");
                return ResponseEntity.badRequest().body(Map.of(VERIFIED, false));
            }

            clientService.verifyClient(clientId, nric);
            logger.debug("Client verified successfully");
            return ResponseEntity.ok(Map.of(VERIFIED, true));
        } catch (ClientNotFoundException | VerificationException e) {
            logger.warn("Verification failed exception occurred: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(VERIFIED, false));
        } catch (Exception e) {
            logger.error("Error verifying client: ", e);
            return ResponseEntity.ok(Map.of(VERIFIED, false));
        }
    }
}
