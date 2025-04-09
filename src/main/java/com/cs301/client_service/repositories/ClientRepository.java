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
    
    @Query(value = "SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "CAST(c.clientId as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.firstName as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.lastName as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.emailAddress as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.phoneNumber as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.address as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.city as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.state as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.country as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.postalCode as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.nric as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.agentId as text) LIKE CONCAT('%', :search, '%'))")
    Page<Client> findAllWithSearch(@Param("search") String search, Pageable pageable);
    
    @Query(value = "SELECT c FROM Client c WHERE " +
           "(:agentId IS NULL OR c.agentId = :agentId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "CAST(c.clientId as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.firstName as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.lastName as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.emailAddress as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.phoneNumber as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.address as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.city as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.state as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.country as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.postalCode as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.nric as text) LIKE CONCAT('%', :search, '%') OR " +
           "CAST(c.agentId as text) LIKE CONCAT('%', :search, '%'))")
    Page<Client> findWithSearchAndAgentId(
            @Param("agentId") String agentId,
            @Param("search") String search,
            Pageable pageable);
}
