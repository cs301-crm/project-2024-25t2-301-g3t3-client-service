package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, String> {
    
    List<Log> findByClientId(String clientId);
    
    List<Log> findByAgentId(String agentId);
    
    List<Log> findByCrudType(Log.CrudType crudType);
    
    List<Log> findByClientIdAndCrudType(String clientId, Log.CrudType crudType);
    
    List<Log> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
