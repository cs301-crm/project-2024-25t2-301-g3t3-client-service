package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, String> {
    List<Client> findByAgentId(String agentId);
    
    Page<Client> findByAgentId(String agentId, Pageable pageable);
    
    @Query("SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR " +
           "LOWER(CAST(c.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.emailAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nric) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> findAllWithSearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT c FROM Client c WHERE " +
           "(:agentId IS NULL OR c.agentId = :agentId) AND " +
           "(:search IS NULL OR " +
           "LOWER(CAST(c.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.emailAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.postalCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.nric) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> findWithSearchAndAgentId(
            @Param("agentId") String agentId,
            @Param("search") String search,
            Pageable pageable);
}
