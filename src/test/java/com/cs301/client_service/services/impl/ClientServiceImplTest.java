package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client testClient;
    private final String clientId = "client-uuid";
    private final String nric = "S1234567A";
    private final String agentId = "agent001";

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = new Client();
        testClient.setClientId(clientId);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testClient.setGender(Gender.MALE);
        testClient.setEmailAddress("john.doe@example.com");
        testClient.setPhoneNumber("1234567890");
        testClient.setAddress("123 Main St");
        testClient.setCity("Singapore");
        testClient.setState("Singapore");
        testClient.setCountry("Singapore");
        testClient.setPostalCode("123456");
        testClient.setNric(nric);
        testClient.setAgentId(agentId);
    }

    @Nested
    @DisplayName("Create Client Tests")
    class CreateClientTests {
        @Test
        @DisplayName("Should successfully create a client")
        void testCreateClient_Success() {
            // Given
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // When
            Client result = clientService.createClient(testClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getClientId()).isEqualTo(clientId);
            assertThat(result.getFirstName()).isEqualTo("John");
            verify(clientRepository, times(1)).save(testClient);
        }
    }

    @Nested
    @DisplayName("Get Client Tests")
    class GetClientTests {
        @Test
        @DisplayName("Should successfully retrieve a client by ID")
        void testGetClient_Success() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

            // When
            Client result = clientService.getClient(clientId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getClientId()).isEqualTo(clientId);
            verify(clientRepository, times(1)).findById(clientId);
        }

        @Test
        @DisplayName("Should throw ClientNotFoundException when client not found")
        void testGetClient_NotFound() {
            // Given
            when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ClientNotFoundException.class, () -> {
                clientService.getClient("non-existent-id");
            });
            verify(clientRepository, times(1)).findById("non-existent-id");
        }
    }

    @Nested
    @DisplayName("Get All Clients Tests")
    class GetAllClientsTests {
        @Test
        @DisplayName("Should retrieve all clients")
        void testGetAllClients_Success() {
            // Given
            Client anotherClient = new Client();
            anotherClient.setClientId("another-uuid");
            anotherClient.setFirstName("Jane");

            when(clientRepository.findAll()).thenReturn(Arrays.asList(testClient, anotherClient));

            // When
            List<Client> results = clientService.getAllClients();

            // Then
            assertThat(results).isNotEmpty();
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getClientId()).isEqualTo(clientId);
            assertThat(results.get(1).getClientId()).isEqualTo("another-uuid");
            verify(clientRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no clients exist")
        void testGetAllClients_EmptyList() {
            // Given
            when(clientRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<Client> results = clientService.getAllClients();

            // Then
            assertThat(results).isEmpty();
            verify(clientRepository, times(1)).findAll();
        }
    }
    
    @Nested
    @DisplayName("Get Clients By Agent ID Tests")
    class GetClientsByAgentIdTests {
        @Test
        @DisplayName("Should retrieve all clients for a specific agent")
        void testGetClientsByAgentId_Success() {
            // Given
            Client anotherClient = new Client();
            anotherClient.setClientId("another-uuid");
            anotherClient.setFirstName("Jane");
            anotherClient.setAgentId(agentId);

            when(clientRepository.findByAgentId(agentId)).thenReturn(Arrays.asList(testClient, anotherClient));

            // When
            List<Client> results = clientService.getClientsByAgentId(agentId);

            // Then
            assertThat(results).isNotEmpty();
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getClientId()).isEqualTo(clientId);
            assertThat(results.get(1).getClientId()).isEqualTo("another-uuid");
            assertThat(results.get(0).getAgentId()).isEqualTo(agentId);
            assertThat(results.get(1).getAgentId()).isEqualTo(agentId);
            verify(clientRepository, times(1)).findByAgentId(agentId);
        }

        @Test
        @DisplayName("Should return empty list when no clients exist for the agent")
        void testGetClientsByAgentId_EmptyList() {
            // Given
            when(clientRepository.findByAgentId("non-existent-agent")).thenReturn(Collections.emptyList());

            // When
            List<Client> results = clientService.getClientsByAgentId("non-existent-agent");

            // Then
            assertThat(results).isEmpty();
            verify(clientRepository, times(1)).findByAgentId("non-existent-agent");
        }
    }

    @Nested
    @DisplayName("Update Client Tests")
    class UpdateClientTests {
        @Test
        @DisplayName("Should successfully update a client")
        void testUpdateClient_Success() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

            Client updatedClient = new Client();
            updatedClient.setClientId(clientId);
            updatedClient.setFirstName("Updated");
            updatedClient.setLastName("Name");

            when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

            // When
            Client result = clientService.updateClient(clientId, updatedClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getClientId()).isEqualTo(clientId);
            assertThat(result.getFirstName()).isEqualTo("Updated");
            assertThat(result.getLastName()).isEqualTo("Name");
            verify(clientRepository, times(1)).findById(clientId);
            verify(clientRepository, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Should throw ClientNotFoundException when updating non-existent client")
        void testUpdateClient_ClientNotFound() {
            // Given
            when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            Client clientToUpdate = new Client();
            assertThrows(ClientNotFoundException.class, () -> {
                clientService.updateClient("non-existent-id", clientToUpdate);
            });
            verify(clientRepository, times(1)).findById("non-existent-id");
            verify(clientRepository, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("Delete Client Tests")
    class DeleteClientTests {
        @Test
        @DisplayName("Should successfully delete a client with no accounts")
        void testDeleteClient_Success() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
            when(accountService.getAccountsByClientId(clientId)).thenReturn(Collections.emptyList());
            doNothing().when(accountService).deleteAccountsByClientId(clientId);
            doNothing().when(clientRepository).deleteById(clientId);

            // When
            clientService.deleteClient(clientId);

            // Then
            verify(clientRepository, times(1)).findById(clientId);
            verify(accountService, times(1)).getAccountsByClientId(clientId);
            verify(accountService, times(1)).deleteAccountsByClientId(clientId);
            verify(clientRepository, times(1)).deleteById(clientId);
        }

        @Test
        @DisplayName("Should throw ClientNotFoundException when deleting non-existent client")
        void testDeleteClient_ClientNotFound() {
            // Given
            when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ClientNotFoundException.class, () -> {
                clientService.deleteClient("non-existent-id");
            });
            verify(clientRepository, times(1)).findById("non-existent-id");
            verify(accountService, never()).getAccountsByClientId(anyString());
            verify(accountService, never()).deleteAccountsByClientId(anyString());
            verify(clientRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("Should throw VerificationException when client has active accounts")
        void testDeleteClient_WithActiveAccounts() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

            Account activeAccount = new Account();
            activeAccount.setAccountStatus(AccountStatus.ACTIVE);
            when(accountService.getAccountsByClientId(clientId)).thenReturn(Arrays.asList(activeAccount));

            // When & Then
            assertThrows(VerificationException.class, () -> {
                clientService.deleteClient(clientId);
            });
            verify(clientRepository, times(1)).findById(clientId);
            verify(accountService, times(1)).getAccountsByClientId(clientId);
            verify(accountService, never()).deleteAccountsByClientId(anyString());
            verify(clientRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("Verify Client Tests")
    class VerifyClientTests {
        @Test
        @DisplayName("Should successfully verify a client with correct NRIC")
        void testVerifyClient_Success() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

            // When & Then - Should not throw exception
            clientService.verifyClient(clientId, nric);
            verify(clientRepository, times(1)).findById(clientId);
        }

        @Test
        @DisplayName("Should throw ClientNotFoundException when verifying non-existent client")
        void testVerifyClient_ClientNotFound() {
            // Given
            when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ClientNotFoundException.class, () -> {
                clientService.verifyClient("non-existent-id", nric);
            });
            verify(clientRepository, times(1)).findById("non-existent-id");
        }

        @Test
        @DisplayName("Should throw VerificationException for incorrect NRIC")
        void testVerifyClient_IncorrectNric() {
            // Given
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

            // When & Then
            assertThrows(VerificationException.class, () -> {
                clientService.verifyClient(clientId, "wrong-nric");
            });
            verify(clientRepository, times(1)).findById(clientId);
        }
    }
}
