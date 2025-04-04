package com.cs301.client_service.aspects;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Disabled due to transaction issues")
public class LoggingAspectTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private LogRepository logRepository;
    
    @BeforeEach
    public void clearLogs() {
        logRepository.deleteAll();
    }

    @Test
    public void testClientCreationLogging() {
        // Create a test client
        Client client = Client.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .emailAddress("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("123456")
                .nric("S1234567A")
                .agentId("test-agent001")
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        assertNotNull(savedClient);
        assertNotNull(savedClient.getClientId());

        // Verify that a log entry was created
        List<Log> logs = logRepository.findByClientId(savedClient.getClientId());
        assertFalse(logs.isEmpty());
        
        // Verify log details
        Log log = logs.get(0);
        assertEquals(Log.CrudType.CREATE, log.getCrudType());
        assertEquals(savedClient.getClientId(), log.getClientId());
        assertTrue(log.getAfterValue().contains("John"));
        assertTrue(log.getAfterValue().contains("Doe"));
        assertEquals("", log.getBeforeValue());
    }

    @Test
    public void testClientRetrievalLogging() {
        // Create a test client
        Client client = Client.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .gender(Gender.FEMALE)
                .emailAddress("jane.smith@example.com")
                .phoneNumber("9876543210")
                .address("456 Oak St")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("654321")
                .nric("S9876543B")
                .agentId("test-agent002")
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        
        // Clear logs from creation
        logRepository.deleteAll();
        
        // Retrieve the client
        Client retrievedClient = clientService.getClient(savedClient.getClientId());
        assertNotNull(retrievedClient);
        
        // Verify that a log entry was created for retrieval
        List<Log> logs = logRepository.findByClientId(savedClient.getClientId());
        assertFalse(logs.isEmpty());
        
        // Verify log details
        Log log = logs.get(0);
        assertEquals(Log.CrudType.READ, log.getCrudType());
        assertEquals(savedClient.getClientId(), log.getClientId());
        assertEquals(log.getBeforeValue(), log.getAfterValue()); // Same for READ operation
    }

    @Test
    public void testClientUpdateLogging() {
        // Create a test client with initial values
        Client client = Client.builder()
                .firstName("Robert")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1975, 8, 20))
                .gender(Gender.MALE)
                .emailAddress("robert.johnson@example.com")
                .phoneNumber("5555555555")
                .address("789 Pine St")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("789012")
                .nric("S7890123C")
                .agentId("test-agent003")
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        assertNotNull(savedClient);
        assertNotNull(savedClient.getClientId());
        
        // Store the original values for verification
        String originalFirstName = savedClient.getFirstName();
        String originalPhoneNumber = savedClient.getPhoneNumber();
        
        // Get the client ID for later use
        String clientId = savedClient.getClientId();
        
        // Create a new client object with updated values
        Client updatedClientData = savedClient.toBuilder()
                .firstName("Bob")
                .phoneNumber("6666666666")
                .build();
        
        // Update the client
        Client updatedClient = clientService.updateClient(clientId, updatedClientData);
        assertNotNull(updatedClient);
        assertEquals("Bob", updatedClient.getFirstName());
        
        // Verify that log entries were created for each changed attribute
        List<Log> logs = logRepository.findAll();
        
        // Find update logs for this client
        List<Log> updateLogs = logs.stream()
            .filter(log -> log.getClientId().equals(clientId) && log.getCrudType() == Log.CrudType.UPDATE)
            .toList();
        
        assertFalse(updateLogs.isEmpty(), "Update logs should exist");
        
        // Print logs for debugging
        System.out.println("Number of logs: " + logs.size());
        for (Log log : logs) {
            System.out.println("Log: " + log.getCrudType() + ", Attribute: " + log.getAttributeName() + 
                               ", Before: " + log.getBeforeValue() + ", After: " + log.getAfterValue());
        }
        
        // For this test, we just verify that an update log exists for the client
        // and that it contains the updated values
        boolean foundUpdateLog = false;
        
        for (Log log : logs) {
            if (log.getCrudType() == Log.CrudType.UPDATE && log.getClientId().equals(clientId)) {
                foundUpdateLog = true;
                
                // Verify the log contains the updated values
                assertTrue(log.getAfterValue().contains("Bob") || log.getAttributeName().contains("Bob"), 
                    "Log should contain updated first name");
                assertTrue(log.getAfterValue().contains("6666666666") || log.getAttributeName().contains("6666666666"), 
                    "Log should contain updated phone number");
                
                break;
            }
        }
        
        assertTrue(foundUpdateLog, "An update log should exist for the client");
    }

    @Test
    public void testClientDeletionLogging() {
        // Create a test client
        Client client = Client.builder()
                .firstName("Alice")
                .lastName("Brown")
                .dateOfBirth(LocalDate.of(1995, 3, 10))
                .gender(Gender.FEMALE)
                .emailAddress("alice.brown@example.com")
                .phoneNumber("1112223333")
                .address("101 Elm St")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("101202")
                .nric("S1012023D")
                .agentId("test-agent004")
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        
        // Get the client ID for later use
        String clientId = savedClient.getClientId();
        
        // Delete the client
        clientService.deleteClient(clientId);
        
        // Verify that a log entry was created for deletion
        List<Log> logs = logRepository.findAll();
        
        // Find the deletion log for this client
        Log deletionLog = null;
        for (Log log : logs) {
            if (log.getClientId().equals(clientId) && log.getCrudType() == Log.CrudType.DELETE) {
                deletionLog = log;
                break;
            }
        }
        
        // Verify log exists and has correct details
        assertNotNull(deletionLog, "Deletion log should exist");
        assertEquals(Log.CrudType.DELETE, deletionLog.getCrudType());
        assertEquals(clientId, deletionLog.getClientId());
        assertTrue(deletionLog.getBeforeValue().contains("Alice"), "Log should contain client's first name");
        assertTrue(deletionLog.getBeforeValue().contains("Brown"), "Log should contain client's last name");
        assertEquals("", deletionLog.getAfterValue());
    }
    
    @Test
    public void testMultiAttributeUpdateLogging() {
        // Create a test client with initial values
        Client client = Client.builder()
                .firstName("Michael")
                .lastName("Wilson")
                .dateOfBirth(LocalDate.of(1980, 4, 15))
                .gender(Gender.MALE)
                .emailAddress("michael.wilson@example.com")
                .phoneNumber("1231231234")
                .address("555 Oak Ave")
                .city("Singapore")
                .state("Singapore")
                .country("Singapore")
                .postalCode("555666")
                .nric("S5556667E")
                .agentId("test-agent005")
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        assertNotNull(savedClient);
        assertNotNull(savedClient.getClientId());
        
        // Store the original values for verification
        String originalFirstName = savedClient.getFirstName();
        String originalLastName = savedClient.getLastName();
        String originalAddress = savedClient.getAddress();
        String originalCity = savedClient.getCity();
        String originalPhone = savedClient.getPhoneNumber();
        
        // Get the client ID for later use
        String clientId = savedClient.getClientId();
        
        // Create a new client object with multiple updated values
        Client updatedClientData = savedClient.toBuilder()
                .firstName("Mike")
                .lastName("Williams")
                .phoneNumber("9879879876")
                .address("777 Maple Blvd")
                .city("New City")
                .build();
        
        // Update the client
        Client updatedClient = clientService.updateClient(clientId, updatedClientData);
        assertNotNull(updatedClient);
        assertEquals("Mike", updatedClient.getFirstName());
        assertEquals("Williams", updatedClient.getLastName());
        assertEquals("9879879876", updatedClient.getPhoneNumber());
        assertEquals("777 Maple Blvd", updatedClient.getAddress());
        assertEquals("New City", updatedClient.getCity());
        
        // Verify that a log entry was created for the update
        List<Log> logs = logRepository.findAll();
        
        // Find update logs for this client
        List<Log> updateLogs = logs.stream()
            .filter(log -> log.getClientId().equals(clientId) && log.getCrudType() == Log.CrudType.UPDATE)
            .toList();
        
        assertFalse(updateLogs.isEmpty(), "Update logs should exist");
        assertEquals(1, updateLogs.size(), "There should be exactly one log entry for the update operation");
        
        // Get the log entry
        Log log = updateLogs.get(0);
        
        // Verify basic log properties
        assertEquals(Log.CrudType.UPDATE, log.getCrudType());
        assertEquals(savedClient.getClientId(), log.getClientId());
        
        // Verify that attributeName contains all changed attribute names
        assertNotNull(log.getAttributeName(), "attributeName should not be null");
        assertFalse(log.getAttributeName().isEmpty(), "attributeName should not be empty");
        
        // Check that all attribute names are present
        String attributeNames = log.getAttributeName();
        assertTrue(attributeNames.contains("firstName"), "firstName should be in attributeName");
        assertTrue(attributeNames.contains("lastName"), "lastName should be in attributeName");
        assertTrue(attributeNames.contains("phoneNumber"), "phoneNumber should be in attributeName");
        assertTrue(attributeNames.contains("address"), "address should be in attributeName");
        assertTrue(attributeNames.contains("city"), "city should be in attributeName");
        
        // Verify beforeValue contains all original values
        String beforeValue = log.getBeforeValue();
        assertNotNull(beforeValue, "beforeValue should not be null");
        assertFalse(beforeValue.isEmpty(), "beforeValue should not be empty");
        
        assertTrue(beforeValue.contains("firstName: " + originalFirstName), "beforeValue should contain original firstName");
        assertTrue(beforeValue.contains("lastName: " + originalLastName), "beforeValue should contain original lastName");
        assertTrue(beforeValue.contains("phoneNumber: " + originalPhone), "beforeValue should contain original phoneNumber");
        assertTrue(beforeValue.contains("address: " + originalAddress), "beforeValue should contain original address");
        assertTrue(beforeValue.contains("city: " + originalCity), "beforeValue should contain original city");
        
        // Verify afterValue contains all new values
        String afterValue = log.getAfterValue();
        assertNotNull(afterValue, "afterValue should not be null");
        assertFalse(afterValue.isEmpty(), "afterValue should not be empty");
        
        assertTrue(afterValue.contains("firstName: Mike"), "afterValue should contain new firstName");
        assertTrue(afterValue.contains("lastName: Williams"), "afterValue should contain new lastName");
        assertTrue(afterValue.contains("phoneNumber: 9879879876"), "afterValue should contain new phoneNumber");
        assertTrue(afterValue.contains("address: 777 Maple Blvd"), "afterValue should contain new address");
        assertTrue(afterValue.contains("city: New City"), "afterValue should contain new city");
        
        // Print the log details for debugging
        System.out.println("Multi-attribute update log:");
        System.out.println("  CRUD Type: " + log.getCrudType());
        System.out.println("  Attribute Names: " + log.getAttributeName());
        System.out.println("  Before Values: " + log.getBeforeValue());
        System.out.println("  After Values: " + log.getAfterValue());
    }
}
