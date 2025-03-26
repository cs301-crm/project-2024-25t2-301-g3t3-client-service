package com.cs301.client_service.aspects;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.impl.ClientServiceImpl;
import com.cs301.client_service.protobuf.C2C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Kafka tests are disabled for unit testing")
public class KafkaLoggingAspectTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientServiceImpl clientService;

    @InjectMocks
    private ClientKafkaLoggingAspect kafkaLoggingAspect;

    @Captor
    private ArgumentCaptor<Object> c2cCaptor;

    private Client testClient;
    private final String TOPIC = "c2c";

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .clientId("test-client-id")
                .firstName("John")
                .lastName("Doe")
                .emailAddress("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("123456")
                .nric("S1234567A")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .agentId("test-agent-id")
                .build();
    }

    @Test
    void testLogAfterClientCreation() {
        // Arrange
        doNothing().when(kafkaProducer).produceMessage(anyString(), any(), anyBoolean());

        // Act
        kafkaLoggingAspect.logAfterClientCreation(null, testClient);

        // Assert
        verify(kafkaProducer).produceMessage(eq(testClient.getClientId()), c2cCaptor.capture(), eq(true));
        C2C capturedC2C = (C2C) c2cCaptor.getValue();
        
        assertEquals("system", capturedC2C.getAgentId());
        assertEquals(testClient.getClientId(), capturedC2C.getClientId());
        assertEquals(testClient.getEmailAddress(), capturedC2C.getClientEmail());
        assertEquals("CREATE", capturedC2C.getCrudType());
        // For CREATE operations, CRUDInfo should be empty
        assertEquals("", capturedC2C.getCrudInfo().getAttribute());
        assertEquals("", capturedC2C.getCrudInfo().getBeforeValue());
        assertEquals("", capturedC2C.getCrudInfo().getAfterValue());
    }

    @Test
    @Disabled("Client update aspect is now disabled")
    void testLogAfterClientUpdate() {
        // This test is disabled because the client update aspect is now disabled
        // The Kafka message is now sent directly in the service method
    }

    @Test
    @Disabled("Client deletion aspect is now disabled")
    void testLogAfterClientDeletion() {
        // This test is disabled because the client deletion aspect is now disabled
        // The Kafka message is now sent directly in the service method
    }
}
