package com.cs301.client_service.controllers;

import com.cs301.client_service.configs.TestSecurityConfig;
import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.services.impl.ClientServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(ClientController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientServiceImpl clientService;

    @MockBean
    private ClientMapper clientMapper;
    
    @MockBean
    private KafkaProducer kafkaProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientDTO clientDTO;
    private Client clientModel;
    private String clientId = "client-uuid";
    private String nric = "S1234567A";
    private String agentId = "agent001";

    @BeforeEach
    void setUp() {
        // Setup client model
        clientModel = new Client();
        clientModel.setClientId(clientId);
        clientModel.setFirstName("John");
        clientModel.setLastName("Doe");
        clientModel.setDateOfBirth(LocalDate.of(1990, 1, 1));
        clientModel.setGender(Gender.MALE);
        clientModel.setEmailAddress("john.doe@example.com");
        clientModel.setPhoneNumber("1234567890");
        clientModel.setAddress("123 Main St");
        clientModel.setCity("Singapore");
        clientModel.setState("Singapore");
        clientModel.setCountry("Singapore");
        clientModel.setPostalCode("123456");
        clientModel.setNric(nric);
        clientModel.setAgentId(agentId);
        clientModel.setAccounts(Collections.emptyList());

        // Setup client DTO
        clientDTO = new ClientDTO();
        clientDTO.setClientId(clientId);
        clientDTO.setFirstName("John");
        clientDTO.setLastName("Doe");
        clientDTO.setDateOfBirth("1990-01-01");
        clientDTO.setGender(Gender.MALE);
        clientDTO.setEmailAddress("john.doe@example.com");
        clientDTO.setPhoneNumber("1234567890");
        clientDTO.setAddress("123 Main St");
        clientDTO.setCity("Singapore");
        clientDTO.setState("Singapore");
        clientDTO.setCountry("Singapore");
        clientDTO.setPostalCode("123456");
        clientDTO.setNric(nric);
        clientDTO.setAgentId(agentId);
        clientDTO.setAccounts(Collections.emptyList());
    }

    @Test
    void testCreateClient_Success() throws Exception {
        // Given
        when(clientMapper.toModel(any(ClientDTO.class))).thenReturn(clientModel);
        when(clientService.createClient(any(Client.class))).thenReturn(clientModel);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId", is(clientId)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.emailAddress", is("john.doe@example.com")));

        verify(clientMapper, times(1)).toModel(any(ClientDTO.class));
        verify(clientService, times(1)).createClient(any(Client.class));
        verify(clientMapper, times(1)).toDto(any(Client.class));
    }

    @Test
    void testGetClient_Success() throws Exception {
        // Given
        when(clientService.getClient(clientId)).thenReturn(clientModel);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/clients/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(clientId)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        verify(clientService, times(1)).getClient(clientId);
        verify(clientMapper, times(1)).toDto(clientModel);
    }

    @Test
    void testGetClient_NotFound() throws Exception {
        // Given
        String nonExistentId = "non-existent-id";
        when(clientService.getClient(anyString())).thenThrow(new ClientNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/v1/clients/{clientId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClient(nonExistentId);
        verify(clientMapper, never()).toDto(any(Client.class));
    }
    
    @Test
    void testGetClientsByAgentId_Success() throws Exception {
        // Given
        Client anotherClient = new Client();
        anotherClient.setClientId("another-uuid");
        anotherClient.setFirstName("Jane");
        anotherClient.setAgentId(agentId);
        
        ClientDTO anotherClientDTO = new ClientDTO();
        anotherClientDTO.setClientId("another-uuid");
        anotherClientDTO.setFirstName("Jane");
        anotherClientDTO.setAgentId(agentId);
        
        when(clientService.getClientsByAgentId(agentId)).thenReturn(java.util.Arrays.asList(clientModel, anotherClient));
        when(clientMapper.toDto(clientModel)).thenReturn(clientDTO);
        when(clientMapper.toDto(anotherClient)).thenReturn(anotherClientDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/clients/agent/{agentId}", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId", is(clientId)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].agentId", is(agentId)))
                .andExpect(jsonPath("$[1].clientId", is("another-uuid")))
                .andExpect(jsonPath("$[1].firstName", is("Jane")))
                .andExpect(jsonPath("$[1].agentId", is(agentId)));

        verify(clientService, times(1)).getClientsByAgentId(agentId);
        verify(clientMapper, times(2)).toDto(any(Client.class));
    }
    
    @Test
    void testGetClientsByAgentId_EmptyList() throws Exception {
        // Given
        when(clientService.getClientsByAgentId("non-existent-agent")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/clients/agent/{agentId}", "non-existent-agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(clientService, times(1)).getClientsByAgentId("non-existent-agent");
        verify(clientMapper, never()).toDto(any(Client.class));
    }

    @Test
    void testUpdateClient_Success() throws Exception {
        // Given
        when(clientMapper.toModel(any(ClientDTO.class))).thenReturn(clientModel);
        when(clientService.updateClient(eq(clientId), any(Client.class))).thenReturn(clientModel);
        when(clientMapper.toDto(any(Client.class))).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/clients/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is(clientId)))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(clientMapper, times(1)).toModel(any(ClientDTO.class));
        verify(clientService, times(1)).updateClient(eq(clientId), any(Client.class));
        verify(clientMapper, times(1)).toDto(any(Client.class));
    }

    @Test
    void testUpdateClient_NotFound() throws Exception {
        // Given
        String nonExistentId = "non-existent-id";
        when(clientMapper.toModel(any(ClientDTO.class))).thenReturn(clientModel);
        when(clientService.updateClient(anyString(), any(Client.class)))
                .thenThrow(new ClientNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(put("/api/v1/clients/{clientId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isNotFound());

        verify(clientMapper, times(1)).toModel(any(ClientDTO.class));
        verify(clientService, times(1)).updateClient(eq(nonExistentId), any(Client.class));
        verify(clientMapper, never()).toDto(any(Client.class));
    }

    @Test
    void testDeleteClient_Success() throws Exception {
        // Given
        doNothing().when(clientService).deleteClient(clientId);

        // When & Then
        mockMvc.perform(delete("/api/v1/clients/{clientId}", clientId))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(clientId);
    }

    @Test
    void testDeleteClient_NotFound() throws Exception {
        // Given
        String nonExistentId = "non-existent-id";
        doThrow(new ClientNotFoundException(nonExistentId))
                .when(clientService).deleteClient(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/v1/clients/{clientId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).deleteClient(nonExistentId);
    }

    @Test
    void testVerifyClient_Success() throws Exception {
        // Given
        doNothing().when(clientService).verifyClient(clientId);

        // When & Then
        mockMvc.perform(post("/api/v1/clients/{clientId}/verify", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)));

        verify(clientService, times(1)).verifyClient(clientId);
    }

    @Test
    void testVerifyClient_ClientNotFound() throws Exception {
        // Given
        String nonExistentId = "non-existent-id";
        doThrow(new ClientNotFoundException(nonExistentId))
                .when(clientService).verifyClient(nonExistentId);

        // When & Then
        mockMvc.perform(post("/api/v1/clients/{clientId}/verify", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.verified", is(false)));

        verify(clientService, times(1)).verifyClient(nonExistentId);
    }
}
