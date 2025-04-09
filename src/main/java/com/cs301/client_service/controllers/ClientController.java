package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.dtos.ClientListDTO;
import com.cs301.client_service.dtos.VerificationResponseDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.JwtAuthorizationUtil;
import com.cs301.client_service.utils.JWTUtil;

import com.cs301.client_service.models.Client;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    }

    /**
     * Create a new client
     * Requires: authenticated user
     */
    @PostMapping
    public ResponseEntity<ClientDTO> createClient(
            Authentication authentication,
            @Valid @RequestBody ClientDTO clientDTO) {
        
        var clientModel = clientMapper.toModel(clientDTO);
        var savedClient = clientService.createClient(clientModel);
        var response = clientMapper.toDto(savedClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all clients with pagination and optional filtering
     * Requires: authenticated user
     * - ROLE_AGENT: only retrieve clients where agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: retrieve everything
     */
    @GetMapping
    public ResponseEntity<List<ClientListDTO>> getAllClients(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String agentId) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Client> clientsPage;
        
        // If user is an agent, only show their clients
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            clientsPage = clientService.getClientsWithSearchAndAgentId(agentIdFromJwt, searchQuery, pageable);
        } 
        // If user is an admin and an agentId is provided, filter by that agentId
        else if (JwtAuthorizationUtil.isAdmin(authentication) && agentId != null && !agentId.isEmpty()) {
            clientsPage = clientService.getClientsWithSearchAndAgentId(agentId, searchQuery, pageable);
        }
        // If user is an admin and no agentId is provided, return all clients
        else {
            clientsPage = clientService.getAllClientsPaginated(pageable, searchQuery);
        }
        
        List<ClientListDTO> clientDTOs = clientMapper.toListDtoList(clientsPage.getContent());
        
        return ResponseEntity.ok(clientDTOs);
    }

    /**
     * Get a client by ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(
            Authentication authentication,
            @PathVariable String clientId) {
        
        Client client = clientService.getClient(clientId);
        
        // Validate if the authenticated user has access to this client
        JwtAuthorizationUtil.validateAgentAccess(authentication, client);
        
        var response = clientMapper.toDto(client);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get clients by agent ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if pathvariable agentId == JWT sub agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ClientListDTO>> getClientsByAgentId(
            Authentication authentication,
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // For agents, only allow accessing their own clients
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            if (!agentIdFromJwt.equals(agentId)) {
                throw new UnauthorizedAccessException("Agent can only view clients assigned to them");
            }
        }
        // Admin can access any agent's clients, no check needed
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Client> clientsPage = clientService.getClientsByAgentIdPaginated(agentId, pageable);
        
        List<ClientListDTO> clientDTOs = clientMapper.toListDtoList(clientsPage.getContent());
        
        return ResponseEntity.ok(clientDTOs);
    }

    /**
     * Update a client
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(
            Authentication authentication,
            @PathVariable String clientId,
            @Valid @RequestBody ClientDTO clientDTO) {
        
        // Validate access before update
        Client existingClient = clientService.getClient(clientId);
        JwtAuthorizationUtil.validateAgentAccess(authentication, existingClient);
        
        var clientModel = clientMapper.toModel(clientDTO);
        var updatedClient = clientService.updateClient(clientId, clientModel);
        var response = clientMapper.toDto(updatedClient);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a client
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(
            Authentication authentication,
            @PathVariable String clientId) {
        
        // Validate access before deletion
        Client existingClient = clientService.getClient(clientId);
        JwtAuthorizationUtil.validateAgentAccess(authentication, existingClient);
        
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verify a client to activate their profile
     * This changes their status from PENDING to VERIFIED
     * No authentication needed
     */
    @PostMapping("/{clientId}/verify")
    public ResponseEntity<VerificationResponseDTO> verifyClient(@PathVariable String clientId) {
        try {
            clientService.verifyClient(clientId);
            return ResponseEntity.ok(VerificationResponseDTO.builder().verified(true).build());
        } catch (ClientNotFoundException ex) {
            // Return a consistent response format for the not found case
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(VerificationResponseDTO.builder().verified(false).build());
        }
    }
}
