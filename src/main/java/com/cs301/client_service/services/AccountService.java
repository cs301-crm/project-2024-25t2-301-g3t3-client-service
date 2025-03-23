package com.cs301.client_service.services;

import com.cs301.client_service.models.Account;
import java.util.List;

public interface AccountService {
    Account createAccount(Account account);
    Account getAccount(String accountId);
    List<Account> getAccountsByClientId(String clientId);
    void deleteAccount(String accountId);
    void deleteAccountsByClientId(String clientId);
}
