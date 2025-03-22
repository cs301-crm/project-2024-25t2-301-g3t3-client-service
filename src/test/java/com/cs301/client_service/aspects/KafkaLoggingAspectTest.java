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
        assertEquals("clientId,firstName,lastName,dateOfBirth,gender,emailAddress,phoneNumber,address,city,state,country,postalCode,nric,agentId", capturedC2C.getCrudInfo().getAttribute());
    }

    @Test
    void testLogAfterClientUpdate() {
        // Arrange
        Client updatedClient = testClient.toBuilder()
                .firstName("Jane")
                .lastName("Smith")
                .build();
        
        when(clientRepository.findById(testClient.getClientId()))
                .thenReturn(Optional.of(testClient));
        doNothing().when(kafkaProducer).produceMessage(anyString(), any(), anyBoolean());

        // Act
        Object[] args = new Object[] { testClient.getClientId(), updatedClient };
        org.aspectj.lang.JoinPoint joinPoint = mock(org.aspectj.lang.JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        kafkaLoggingAspect.logAfterClientUpdate(joinPoint, updatedClient);

        // Assert
        verify(kafkaProducer).produceMessage(eq(testClient.getClientId()), c2cCaptor.capture(), eq(true));
        C2C capturedC2C = (C2C) c2cCaptor.getValue();
        
        assertEquals(testClient.getEmailAddress(), capturedC2C.getClientEmail());
        assertEquals("UPDATE", capturedC2C.getCrudType());
    }

    @Test
    void testLogAfterClientDeletion() {
        // Arrange
        when(clientRepository.findById(testClient.getClientId()))
                .thenReturn(Optional.of(testClient));
        doNothing().when(kafkaProducer).produceMessage(anyString(), any(), anyBoolean());

        // Act
        Object[] args = new Object[] { testClient.getClientId() };
        org.aspectj.lang.JoinPoint joinPoint = mock(org.aspectj.lang.JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        kafkaLoggingAspect.logAfterClientDeletion(joinPoint);

        // Assert
        verify(kafkaProducer).produceMessage(eq(testClient.getClientId()), c2cCaptor.capture(), eq(true));
        C2C capturedC2C = (C2C) c2cCaptor.getValue();
        
        assertEquals(testClient.getEmailAddress(), capturedC2C.getClientEmail());
        assertEquals("DELETE", capturedC2C.getCrudType());
    }
}
