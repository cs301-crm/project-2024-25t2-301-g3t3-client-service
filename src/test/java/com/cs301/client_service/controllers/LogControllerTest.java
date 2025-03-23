package com.cs301.client_service.controllers;

import com.cs301.client_service.configs.TestSecurityConfig;
import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.mappers.LogMapper;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogRepository logRepository;

    @MockBean
    private LogMapper logMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Log clientProfileLog;
    private Log accountLog;
    private LogDTO clientProfileLogDTO;
    private LogDTO accountLogDTO;
    private String clientId = "c1000000-0000-0000-0000-000000000001";
    private String agentId = "test-agent001";

    @BeforeEach
    void setUp() {
        // Setup client profile log
        clientProfileLog = Log.builder()
                .id("l1000000-0000-0000-0000-000000000001")
                .clientId(clientId)
                .agentId(agentId)
                .crudType(Log.CrudType.CREATE)
                .attributeName(clientId)
                .beforeValue("")
                .afterValue("{\"clientId\":\"" + clientId + "\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
                .dateTime(LocalDateTime.now())
                .build();

        // Setup account log
        accountLog = Log.builder()
                .id("l5000000-0000-0000-0000-000000000005")
                .clientId(clientId)
                .agentId(agentId)
                .crudType(Log.CrudType.CREATE)
                .attributeName("a1000000-0000-0000-0000-000000000001")
                .beforeValue("")
                .afterValue("{\"accountId\":\"a1000000-0000-0000-0000-000000000001\",\"clientId\":\"" + clientId + "\",\"accountType\":\"SAVINGS\"}")
                .dateTime(LocalDateTime.now())
                .build();

        // Setup client profile log DTO
        clientProfileLogDTO = LogDTO.builder()
                .id("l1000000-0000-0000-0000-000000000001")
                .clientId(clientId)
                .agentId(agentId)
                .message("Created profile for " + clientId)
                .attributeName(clientId)
                .beforeValue("")
                .afterValue("{\"clientId\":\"" + clientId + "\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
                .dateTime(LocalDateTime.now())
                .crudType("CREATE")
                .build();

        // Setup account log DTO
        accountLogDTO = LogDTO.builder()
                .id("l5000000-0000-0000-0000-000000000005")
                .clientId(clientId)
                .agentId(agentId)
                .message("Created account for " + clientId)
                .attributeName("a1000000-0000-0000-0000-000000000001")
                .beforeValue("")
                .afterValue("{\"accountId\":\"a1000000-0000-0000-0000-000000000001\",\"clientId\":\"" + clientId + "\",\"accountType\":\"SAVINGS\"}")
                .dateTime(LocalDateTime.now())
                .crudType("CREATE")
                .build();
    }

    @Test
    void testGetAllLogs_Success() throws Exception {
        // Given
        List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
        List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
        when(logRepository.findAll()).thenReturn(logs);
        when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

        // When & Then
        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("l1000000-0000-0000-0000-000000000001")))
                .andExpect(jsonPath("$[0].clientId", is(clientId)))
                .andExpect(jsonPath("$[0].message", is("Created profile for " + clientId)))
                .andExpect(jsonPath("$[1].id", is("l5000000-0000-0000-0000-000000000005")))
                .andExpect(jsonPath("$[1].message", is("Created account for " + clientId)));

        verify(logRepository, times(1)).findAll();
        verify(logMapper, times(1)).toDTOList(logs);
    }

    @Test
    void testGetLogsByClientId_Success() throws Exception {
        // Given
        List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
        List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
        when(logRepository.findByClientId(clientId)).thenReturn(logs);
        when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

        // When & Then
        mockMvc.perform(get("/api/logs/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("l1000000-0000-0000-0000-000000000001")))
                .andExpect(jsonPath("$[0].clientId", is(clientId)))
                .andExpect(jsonPath("$[0].message", is("Created profile for " + clientId)))
                .andExpect(jsonPath("$[1].id", is("l5000000-0000-0000-0000-000000000005")))
                .andExpect(jsonPath("$[1].message", is("Created account for " + clientId)));

        verify(logRepository, times(1)).findByClientId(clientId);
        verify(logMapper, times(1)).toDTOList(logs);
    }

    @Test
    void testGetLogsByClientId_NoLogs() throws Exception {
        // Given
        String nonExistentClientId = "non-existent-client";
        when(logRepository.findByClientId(nonExistentClientId)).thenReturn(Arrays.asList());
        when(logMapper.toDTOList(Arrays.asList())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/logs/client/{clientId}", nonExistentClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logRepository, times(1)).findByClientId(nonExistentClientId);
        verify(logMapper, times(1)).toDTOList(Arrays.asList());
    }

    @Test
    void testGetLogsByCrudType_Success() throws Exception {
        // Given
        List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
        List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
        when(logRepository.findByCrudType(Log.CrudType.CREATE)).thenReturn(logs);
        when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

        // When & Then
        mockMvc.perform(get("/api/logs/type/{crudType}", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("l1000000-0000-0000-0000-000000000001")))
                .andExpect(jsonPath("$[0].crudType", is("CREATE")))
                .andExpect(jsonPath("$[1].id", is("l5000000-0000-0000-0000-000000000005")))
                .andExpect(jsonPath("$[1].crudType", is("CREATE")));

        verify(logRepository, times(1)).findByCrudType(Log.CrudType.CREATE);
        verify(logMapper, times(1)).toDTOList(logs);
    }

    @Test
    void testGetLogsByCrudType_NoLogs() throws Exception {
        // Given
        when(logRepository.findByCrudType(Log.CrudType.DELETE)).thenReturn(Arrays.asList());
        when(logMapper.toDTOList(Arrays.asList())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/logs/type/{crudType}", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logRepository, times(1)).findByCrudType(Log.CrudType.DELETE);
        verify(logMapper, times(1)).toDTOList(Arrays.asList());
    }
    
    @Test
    void testGetLogsByAgentId_Success() throws Exception {
        // Given
        List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
        List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
        when(logRepository.findByAgentId(agentId)).thenReturn(logs);
        when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

        // When & Then
        mockMvc.perform(get("/api/logs/agent/{agentId}", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("l1000000-0000-0000-0000-000000000001")))
                .andExpect(jsonPath("$[0].agentId", is(agentId)))
                .andExpect(jsonPath("$[0].message", is("Created profile for " + clientId)))
                .andExpect(jsonPath("$[1].id", is("l5000000-0000-0000-0000-000000000005")))
                .andExpect(jsonPath("$[1].message", is("Created account for " + clientId)));

        verify(logRepository, times(1)).findByAgentId(agentId);
        verify(logMapper, times(1)).toDTOList(logs);
    }

    @Test
    void testGetLogsByAgentId_NoLogs() throws Exception {
        // Given
        String nonExistentAgentId = "non-existent-agent";
        when(logRepository.findByAgentId(nonExistentAgentId)).thenReturn(Arrays.asList());
        when(logMapper.toDTOList(Arrays.asList())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/logs/agent/{agentId}", nonExistentAgentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(logRepository, times(1)).findByAgentId(nonExistentAgentId);
        verify(logMapper, times(1)).toDTOList(Arrays.asList());
    }
}
