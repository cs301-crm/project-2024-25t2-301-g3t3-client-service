package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByClientClientId(String clientId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.client.clientId = :clientId")
    void deleteByClientClientId(@Param("clientId") String clientId);
}
