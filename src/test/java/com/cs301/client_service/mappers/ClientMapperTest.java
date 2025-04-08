package com.cs301.client_service.mappers;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.dtos.ClientListDTO;
import com.cs301.client_service.models.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClientMapperTest {

    @Autowired
    private ClientMapper clientMapper;

    private Client clientModel;
    private ClientDTO clientDTO;
    private final String clientId = "client-uuid";
    private final String nric = "S1234567A";
    private final String agentId = "agent001";

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
        clientModel.setVerificationStatus(VerificationStatus.PENDING);

        // Setup client DTO
        clientDTO = ClientDTO.builder()
                .clientId(clientId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .gender(Gender.MALE)
                .emailAddress("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("123456")
                .nric(nric)
                .agentId(agentId)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
    }

    @Test
    void testToDto() {
        // Act
        ClientDTO result = clientMapper.toDto(clientModel);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("1990-01-01", result.getDateOfBirth());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals("john.doe@example.com", result.getEmailAddress());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Singapore", result.getCity());
        assertEquals("Singapore", result.getState());
        assertEquals("Singapore", result.getCountry());
        assertEquals("123456", result.getPostalCode());
        assertEquals(nric, result.getNric());
        assertEquals(agentId, result.getAgentId());
        assertEquals(VerificationStatus.PENDING, result.getVerificationStatus());
    }

    @Test
    void testToModel() {
        // Act
        Client result = clientMapper.toModel(clientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals("john.doe@example.com", result.getEmailAddress());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Singapore", result.getCity());
        assertEquals("Singapore", result.getState());
        assertEquals("Singapore", result.getCountry());
        assertEquals("123456", result.getPostalCode());
        assertEquals(nric, result.getNric());
        assertEquals(agentId, result.getAgentId());
        assertEquals(VerificationStatus.PENDING, result.getVerificationStatus());
    }

    @Test
    void testToListDto() {
        // Act
        ClientListDTO result = clientMapper.toListDto(clientModel);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    @Test
    void testToListDtoList() {
        // Arrange
        List<Client> clients = Arrays.asList(clientModel);

        // Act
        List<ClientListDTO> result = clientMapper.toListDtoList(clients);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ClientListDTO firstResult = result.get(0);
        assertEquals(clientId, firstResult.getClientId());
        assertEquals("John", firstResult.getFirstName());
        assertEquals("Doe", firstResult.getLastName());
    }

    @Test
    void testToDtoWithNullModel() {
        // Act
        ClientDTO result = clientMapper.toDto(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToModelWithNullDto() {
        // Act
        Client result = clientMapper.toModel(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToModelWithNullVerificationStatus() {
        // Arrange
        clientDTO.setVerificationStatus(null);

        // Act
        Client result = clientMapper.toModel(clientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(VerificationStatus.PENDING, result.getVerificationStatus());
    }
} 