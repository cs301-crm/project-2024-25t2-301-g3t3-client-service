package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByClientClientId(String clientId);
    void deleteByClientClientId(String clientId);
}