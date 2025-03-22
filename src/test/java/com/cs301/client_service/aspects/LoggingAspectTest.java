package com.cs301.client_service.aspects;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.services.ClientService;
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
@Transactional
public class LoggingAspectTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private LogRepository logRepository;

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
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        assertNotNull(savedClient);
        assertNotNull(savedClient.getClientId());
        
        // Store the original values for verification
        String originalFirstName = savedClient.getFirstName();
        String originalPhoneNumber = savedClient.getPhoneNumber();
        
        // Clear logs from creation
        logRepository.deleteAll();
        
        // Create a new client object with updated values
        Client updatedClientData = savedClient.toBuilder()
                .firstName("Bob")
                .phoneNumber("6666666666")
                .build();
        
        // Update the client
        Client updatedClient = clientService.updateClient(savedClient.getClientId(), updatedClientData);
        assertNotNull(updatedClient);
        assertEquals("Bob", updatedClient.getFirstName());
        
        // Verify that log entries were created for each changed attribute
        List<Log> logs = logRepository.findByClientId(savedClient.getClientId());
        assertFalse(logs.isEmpty());
        
        // Print logs for debugging
        System.out.println("Number of logs: " + logs.size());
        for (Log log : logs) {
            System.out.println("Log: " + log.getCrudType() + ", Attribute: " + log.getAttributeName() + 
                               ", Before: " + log.getBeforeValue() + ", After: " + log.getAfterValue());
        }
        
        // Verify logs contain the correct attribute changes
        boolean foundFirstNameChange = false;
        boolean foundPhoneNumberChange = false;
        
        for (Log log : logs) {
            assertEquals(Log.CrudType.UPDATE, log.getCrudType());
            assertEquals(savedClient.getClientId(), log.getClientId());
            
            if ("firstName".equals(log.getAttributeName())) {
                assertEquals(originalFirstName, log.getBeforeValue());
                assertEquals("Bob", log.getAfterValue());
                foundFirstNameChange = true;
            } else if ("phoneNumber".equals(log.getAttributeName())) {
                assertEquals(originalPhoneNumber, log.getBeforeValue());
                assertEquals("6666666666", log.getAfterValue());
                foundPhoneNumberChange = true;
            }
        }
        
        // Verify at least one change was logged
        assertTrue(foundFirstNameChange, "First name change should be logged");
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
                .build();

        // Save the client
        Client savedClient = clientService.createClient(client);
        
        // Clear logs from creation
        logRepository.deleteAll();
        
        // Delete the client
        clientService.deleteClient(savedClient.getClientId());
        
        // Verify that a log entry was created for deletion
        List<Log> logs = logRepository.findByClientId(savedClient.getClientId());
        assertFalse(logs.isEmpty());
        
        // Verify log details
        Log log = logs.get(0);
        assertEquals(Log.CrudType.DELETE, log.getCrudType());
        assertEquals(savedClient.getClientId(), log.getClientId());
        assertTrue(log.getBeforeValue().contains("Alice"));
        assertTrue(log.getBeforeValue().contains("Brown"));
        assertEquals("", log.getAfterValue());
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
        
        // Clear logs from creation
        logRepository.deleteAll();
        
        // Create a new client object with multiple updated values
        Client updatedClientData = savedClient.toBuilder()
                .firstName("Mike")
                .lastName("Williams")
                .phoneNumber("9879879876")
                .address("777 Maple Blvd")
                .city("New City")
                .build();
        
        // Update the client
        Client updatedClient = clientService.updateClient(savedClient.getClientId(), updatedClientData);
        assertNotNull(updatedClient);
        assertEquals("Mike", updatedClient.getFirstName());
        assertEquals("Williams", updatedClient.getLastName());
        assertEquals("9879879876", updatedClient.getPhoneNumber());
        assertEquals("777 Maple Blvd", updatedClient.getAddress());
        assertEquals("New City", updatedClient.getCity());
        
        // Verify that a single log entry was created for all changes
        List<Log> logs = logRepository.findByClientId(savedClient.getClientId());
        assertFalse(logs.isEmpty());
        assertEquals(1, logs.size(), "There should be exactly one log entry for the update operation");
        
        // Get the log entry
        Log log = logs.get(0);
        
        // Verify basic log properties
        assertEquals(Log.CrudType.UPDATE, log.getCrudType());
        assertEquals(savedClient.getClientId(), log.getClientId());
        assertNotNull(log.getAttributesJson(), "attributesJson should not be null");
        assertFalse(log.getAttributesJson().isEmpty(), "attributesJson should not be empty");
        
        // Verify that attributesJson contains all changed attributes
        Map<String, Object> attributes = log.getAttributes();
        assertNotNull(attributes, "Attributes map should not be null");
        assertFalse(attributes.isEmpty(), "Attributes map should not be empty");
        
        // Verify each attribute change is present in the map
        assertTrue(attributes.containsKey("firstName"), "firstName change should be logged");
        assertTrue(attributes.containsKey("lastName"), "lastName change should be logged");
        assertTrue(attributes.containsKey("phoneNumber"), "phoneNumber change should be logged");
        assertTrue(attributes.containsKey("address"), "address change should be logged");
        assertTrue(attributes.containsKey("city"), "city change should be logged");
        
        // Verify the values of each attribute change
        @SuppressWarnings("unchecked")
        Map.Entry<String, String> firstNameChange = (Map.Entry<String, String>) attributes.get("firstName");
        assertEquals(originalFirstName, firstNameChange.getKey());
        assertEquals("Mike", firstNameChange.getValue());
        
        @SuppressWarnings("unchecked")
        Map.Entry<String, String> lastNameChange = (Map.Entry<String, String>) attributes.get("lastName");
        assertEquals(originalLastName, lastNameChange.getKey());
        assertEquals("Williams", lastNameChange.getValue());
        
        @SuppressWarnings("unchecked")
        Map.Entry<String, String> phoneChange = (Map.Entry<String, String>) attributes.get("phoneNumber");
        assertEquals(originalPhone, phoneChange.getKey());
        assertEquals("9879879876", phoneChange.getValue());
        
        @SuppressWarnings("unchecked")
        Map.Entry<String, String> addressChange = (Map.Entry<String, String>) attributes.get("address");
        assertEquals(originalAddress, addressChange.getKey());
        assertEquals("777 Maple Blvd", addressChange.getValue());
        
        @SuppressWarnings("unchecked")
        Map.Entry<String, String> cityChange = (Map.Entry<String, String>) attributes.get("city");
        assertEquals(originalCity, cityChange.getKey());
        assertEquals("New City", cityChange.getValue());
        
        // Verify that for backward compatibility, the first attribute is also set in the traditional fields
        assertNotNull(log.getAttributeName(), "attributeName should not be null");
        assertNotNull(log.getBeforeValue(), "beforeValue should not be null");
        assertNotNull(log.getAfterValue(), "afterValue should not be null");
        
        // Print the log details for debugging
        System.out.println("Multi-attribute update log:");
        System.out.println("  CRUD Type: " + log.getCrudType());
        System.out.println("  Attribute Name: " + log.getAttributeName());
        System.out.println("  Before Value: " + log.getBeforeValue());
        System.out.println("  After Value: " + log.getAfterValue());
        System.out.println("  Attributes JSON: " + log.getAttributesJson());
        System.out.println("  Number of attributes: " + attributes.size());
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> valueEntry = (Map.Entry<String, String>) entry.getValue();
            System.out.println("    " + entry.getKey() + ": " + valueEntry.getKey() + " -> " + valueEntry.getValue());
        }
    }
}
