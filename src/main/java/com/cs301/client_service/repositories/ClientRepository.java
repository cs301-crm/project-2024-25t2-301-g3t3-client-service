package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, String> {
    List<Client> findByAgentId(String agentId);
}
