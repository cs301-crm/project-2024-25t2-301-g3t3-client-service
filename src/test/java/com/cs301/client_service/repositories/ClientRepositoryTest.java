package com.cs301.client_service.repositories;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.models.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient;

    @BeforeEach
    void setup() {
        // Create a client
        testClient = new Client();
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
        testClient.setNric("S1234567A");

        // Persist the client
        entityManager.persist(testClient);
        entityManager.flush();
    }

    @Test
    void testFindById() {
        // When: finding client by ID
        Optional<Client> foundClient = clientRepository.findById(testClient.getClientId());

        // Then: the client should be found
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getFirstName()).isEqualTo("John");
        assertThat(foundClient.get().getLastName()).isEqualTo("Doe");
        assertThat(foundClient.get().getEmailAddress()).isEqualTo("john.doe@example.com");
        assertThat(foundClient.get().getNric()).isEqualTo("S1234567A");
    }

    @Test
    void testFindById_NotFound() {
        // When: finding a client with a non-existent ID
        Optional<Client> foundClient = clientRepository.findById("non-existent-id");

        // Then: no client should be found
        assertThat(foundClient).isEmpty();
    }

    @Test
    void testSaveClient() {
        // Given: a new client
        Client newClient = new Client();
        newClient.setFirstName("Jane");
        newClient.setLastName("Smith");
        newClient.setDateOfBirth(LocalDate.of(1992, 2, 2));
        newClient.setGender(Gender.FEMALE);
        newClient.setEmailAddress("jane.smith@example.com");
        newClient.setPhoneNumber("0987654321");
        newClient.setAddress("456 Side St");
        newClient.setCity("Singapore");
        newClient.setState("Singapore");
        newClient.setCountry("Singapore");
        newClient.setPostalCode("654321");
        newClient.setNric("S7654321A");

        // When: saving the client
        Client savedClient = clientRepository.save(newClient);

        // Then: the client should be saved with an ID
        assertThat(savedClient.getClientId()).isNotNull();
        assertThat(savedClient.getFirstName()).isEqualTo("Jane");
        assertThat(savedClient.getLastName()).isEqualTo("Smith");

        // And: the client should be retrievable from the database
        Optional<Client> retrievedClient = clientRepository.findById(savedClient.getClientId());
        assertThat(retrievedClient).isPresent();
        assertThat(retrievedClient.get().getEmailAddress()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void testDeleteClient() {
        // When: deleting a client
        clientRepository.deleteById(testClient.getClientId());
        entityManager.flush();

        // Then: the client should no longer be in the database
        Optional<Client> deletedClient = clientRepository.findById(testClient.getClientId());
        assertThat(deletedClient).isEmpty();
    }
}