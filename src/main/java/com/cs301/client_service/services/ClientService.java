package com.cs301.client_service.services;

import com.cs301.client_service.models.Client;
import java.util.List;

public interface ClientService {
    Client createClient(Client client);
    Client getClient(String clientId);
    List<Client> getAllClients();
    Client updateClient(String clientId, Client client);
    void deleteClient(String clientId);
    void verifyClient(String clientId, String nric);
}