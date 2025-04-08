// package com.cs301.client_service.controllers;

// import com.cs301.client_service.configs.TestSecurityConfig;
// import com.cs301.client_service.dtos.LogDTO;
// import com.cs301.client_service.dtos.PaginatedResponse;
// import com.cs301.client_service.mappers.LogMapper;
// import com.cs301.client_service.models.Log;
// import com.cs301.client_service.repositories.LogRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;

// import static org.hamcrest.Matchers.hasSize;
// import static org.hamcrest.Matchers.is;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(LogController.class)
// @Import(TestSecurityConfig.class)
// @ActiveProfiles("test")
// @Disabled("Disabled for unit testing")
// class LogControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private LogRepository logRepository;

//     @MockBean
//     private LogMapper logMapper;

//     @Autowired
//     private ObjectMapper objectMapper;

//     private Log clientProfileLog;
//     private Log accountLog;
//     private LogDTO clientProfileLogDTO;
//     private LogDTO accountLogDTO;
//     private String clientId = "c1000000-0000-0000-0000-000000000001";
//     private String agentId = "test-agent001";

//     @BeforeEach
//     void setUp() {
//         // Setup client profile log
//         clientProfileLog = Log.builder()
//                 .id("l1000000-0000-0000-0000-000000000001")
//                 .clientId(clientId)
//                 .agentId(agentId)
//                 .crudType(Log.CrudType.CREATE)
//                 .attributeName(clientId)
//                 .beforeValue("")
//                 .afterValue("{\"clientId\":\"" + clientId + "\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
//                 .dateTime(LocalDateTime.now())
//                 .build();

//         // Setup account log
//         accountLog = Log.builder()
//                 .id("l5000000-0000-0000-0000-000000000005")
//                 .clientId(clientId)
//                 .agentId(agentId)
//                 .crudType(Log.CrudType.CREATE)
//                 .attributeName("a1000000-0000-0000-0000-000000000001")
//                 .beforeValue("")
//                 .afterValue("{\"accountId\":\"a1000000-0000-0000-0000-000000000001\",\"clientId\":\"" + clientId + "\",\"accountType\":\"SAVINGS\"}")
//                 .dateTime(LocalDateTime.now())
//                 .build();

//         // Setup client profile log DTO
//         clientProfileLogDTO = LogDTO.builder()
//                 .id("l1000000-0000-0000-0000-000000000001")
//                 .clientId(clientId)
//                 .clientName("Client " + clientId.substring(0, 8))
//                 .agentId(agentId)
//                 .message("Created profile for " + clientId)
//                 .attributeName(clientId)
//                 .beforeValue("")
//                 .afterValue("{\"clientId\":\"" + clientId + "\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
//                 .dateTime(LocalDateTime.now().toString())
//                 .crudType("CREATE")
//                 .build();

//         // Setup account log DTO
//         accountLogDTO = LogDTO.builder()
//                 .id("l5000000-0000-0000-0000-000000000005")
//                 .clientId(clientId)
//                 .clientName("Client " + clientId.substring(0, 8))
//                 .agentId(agentId)
//                 .message("Created account for " + clientId)
//                 .attributeName("a1000000-0000-0000-0000-000000000001")
//                 .beforeValue("")
//                 .afterValue("{\"accountId\":\"a1000000-0000-0000-0000-000000000001\",\"clientId\":\"" + clientId + "\",\"accountType\":\"SAVINGS\"}")
//                 .dateTime(LocalDateTime.now().toString())
//                 .crudType("CREATE")
//                 .build();
//     }

//     @Test
//     void testGetAllLogs_Success() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
        
//         List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
//         Page<Log> logPage = new PageImpl<>(logs, PageRequest.of(page - 1, limit), 2);
        
//         List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
//         when(logRepository.findAll(any(Pageable.class))).thenReturn(logPage);
//         when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs")
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data", hasSize(2)))
//                 .andExpect(jsonPath("$.data[0].id", is("l1000000-0000-0000-0000-000000000001")))
//                 .andExpect(jsonPath("$.data[0].clientId", is(clientId)))
//                 .andExpect(jsonPath("$.data[0].message", is("Created profile for " + clientId)))
//                 .andExpect(jsonPath("$.data[1].id", is("l5000000-0000-0000-0000-000000000005")))
//                 .andExpect(jsonPath("$.data[1].message", is("Created account for " + clientId)))
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(2)))
//                 .andExpect(jsonPath("$.totalPages", is(1)));

//         verify(logRepository, times(1)).findAll(any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(logs);
//     }

//     @Test
//     void testGetLogsByClientId_Success() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
        
//         List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
//         Page<Log> logPage = new PageImpl<>(logs, PageRequest.of(page - 1, limit), 2);
        
//         List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
//         when(logRepository.findByClientId(eq(clientId), any(Pageable.class))).thenReturn(logPage);
//         when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/client/{clientId}", clientId)
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data", hasSize(2)))
//                 .andExpect(jsonPath("$.data[0].id", is("l1000000-0000-0000-0000-000000000001")))
//                 .andExpect(jsonPath("$.data[0].clientId", is(clientId)))
//                 .andExpect(jsonPath("$.data[0].message", is("Created profile for " + clientId)))
//                 .andExpect(jsonPath("$.data[1].id", is("l5000000-0000-0000-0000-000000000005")))
//                 .andExpect(jsonPath("$.data[1].message", is("Created account for " + clientId)))
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(2)))
//                 .andExpect(jsonPath("$.totalPages", is(1)));

//         verify(logRepository, times(1)).findByClientId(eq(clientId), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(logs);
//     }

//     @Test
//     void testGetLogsByClientId_NoLogs() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
//         String nonExistentClientId = "non-existent-client";
        
//         Page<Log> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, limit), 0);
        
//         when(logRepository.findByClientId(eq(nonExistentClientId), any(Pageable.class))).thenReturn(emptyPage);
//         when(logMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/client/{clientId}", nonExistentClientId)
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty())
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(0)))
//                 .andExpect(jsonPath("$.totalPages", is(0)));

//         verify(logRepository, times(1)).findByClientId(eq(nonExistentClientId), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(Collections.emptyList());
//     }

//     @Test
//     void testGetLogsByCrudType_Success() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
        
//         List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
//         Page<Log> logPage = new PageImpl<>(logs, PageRequest.of(page - 1, limit), 2);
        
//         List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
//         when(logRepository.findByCrudType(eq(Log.CrudType.CREATE), any(Pageable.class))).thenReturn(logPage);
//         when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/type/{crudType}", "CREATE")
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data", hasSize(2)))
//                 .andExpect(jsonPath("$.data[0].id", is("l1000000-0000-0000-0000-000000000001")))
//                 .andExpect(jsonPath("$.data[0].crudType", is("CREATE")))
//                 .andExpect(jsonPath("$.data[1].id", is("l5000000-0000-0000-0000-000000000005")))
//                 .andExpect(jsonPath("$.data[1].crudType", is("CREATE")))
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(2)))
//                 .andExpect(jsonPath("$.totalPages", is(1)));

//         verify(logRepository, times(1)).findByCrudType(eq(Log.CrudType.CREATE), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(logs);
//     }

//     @Test
//     void testGetLogsByCrudType_NoLogs() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
        
//         Page<Log> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, limit), 0);
        
//         when(logRepository.findByCrudType(eq(Log.CrudType.DELETE), any(Pageable.class))).thenReturn(emptyPage);
//         when(logMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/type/{crudType}", "DELETE")
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty())
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(0)))
//                 .andExpect(jsonPath("$.totalPages", is(0)));

//         verify(logRepository, times(1)).findByCrudType(eq(Log.CrudType.DELETE), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(Collections.emptyList());
//     }
    
//     @Test
//     void testGetLogsByAgentId_Success() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
        
//         List<Log> logs = Arrays.asList(clientProfileLog, accountLog);
//         Page<Log> logPage = new PageImpl<>(logs, PageRequest.of(page - 1, limit), 2);
        
//         List<LogDTO> logDTOs = Arrays.asList(clientProfileLogDTO, accountLogDTO);
        
//         when(logRepository.findByAgentId(eq(agentId), any(Pageable.class))).thenReturn(logPage);
//         when(logMapper.toDTOList(logs)).thenReturn(logDTOs);

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/agent/{agentId}", agentId)
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data", hasSize(2)))
//                 .andExpect(jsonPath("$.data[0].id", is("l1000000-0000-0000-0000-000000000001")))
//                 .andExpect(jsonPath("$.data[0].agentId", is(agentId)))
//                 .andExpect(jsonPath("$.data[0].message", is("Created profile for " + clientId)))
//                 .andExpect(jsonPath("$.data[1].id", is("l5000000-0000-0000-0000-000000000005")))
//                 .andExpect(jsonPath("$.data[1].message", is("Created account for " + clientId)))
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(2)))
//                 .andExpect(jsonPath("$.totalPages", is(1)));

//         verify(logRepository, times(1)).findByAgentId(eq(agentId), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(logs);
//     }

//     @Test
//     void testGetLogsByAgentId_NoLogs() throws Exception {
//         // Given
//         int page = 1;
//         int limit = 10;
//         String nonExistentAgentId = "non-existent-agent";
        
//         Page<Log> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page - 1, limit), 0);
        
//         when(logRepository.findByAgentId(eq(nonExistentAgentId), any(Pageable.class))).thenReturn(emptyPage);
//         when(logMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

//         // When & Then
//         mockMvc.perform(get("/api/v1/client-logs/agent/{agentId}", nonExistentAgentId)
//                 .param("page", String.valueOf(page))
//                 .param("limit", String.valueOf(limit)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty())
//                 .andExpect(jsonPath("$.page", is(page)))
//                 .andExpect(jsonPath("$.limit", is(limit)))
//                 .andExpect(jsonPath("$.totalItems", is(0)))
//                 .andExpect(jsonPath("$.totalPages", is(0)));

//         verify(logRepository, times(1)).findByAgentId(eq(nonExistentAgentId), any(Pageable.class));
//         verify(logMapper, times(1)).toDTOList(Collections.emptyList());
//     }
// }
