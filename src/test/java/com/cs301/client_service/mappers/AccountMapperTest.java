package com.cs301.client_service.mappers;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    private Account accountModel;
    private AccountDTO accountDTO;
    private String accountId = "account-uuid";
    private String clientId = "client-uuid";
    private String clientFirstName = "John";
    private String clientLastName = "Doe";

    @BeforeEach
    void setUp() {
        // Setup client
        Client client = new Client();
        client.setClientId(clientId);
        client.setFirstName(clientFirstName);
        client.setLastName(clientLastName);

        // Setup account model
        accountModel = new Account();
        accountModel.setAccountId(accountId);
        accountModel.setClient(client);
        accountModel.setAccountType(AccountType.SAVINGS);
        accountModel.setAccountStatus(AccountStatus.ACTIVE);
        accountModel.setOpeningDate(LocalDate.now());
        accountModel.setInitialDeposit(new BigDecimal("1000.00"));
        accountModel.setCurrency("SGD");
        accountModel.setBranchId("BR001");

        // Setup account DTO
        accountDTO = AccountDTO.builder()
                .accountId(accountId)
                .clientId(clientId)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .openingDate(LocalDate.now().toString())
                .initialDeposit(new BigDecimal("1000.00"))
                .currency("SGD")
                .branchId("BR001")
                .build();
    }

    @Test
    void testToDto() {
        // Act
        AccountDTO result = accountMapper.toDto(accountModel);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(clientId, result.getClientId());
        assertEquals(clientFirstName + " " + clientLastName, result.getClientName());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        assertEquals(accountModel.getOpeningDate().toString(), result.getOpeningDate());
        assertEquals(new BigDecimal("1000.00"), result.getInitialDeposit());
        assertEquals("SGD", result.getCurrency());
        assertEquals("BR001", result.getBranchId());
    }

    @Test
    void testToModel() {
        // Act
        Account result = accountMapper.toModel(accountDTO);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertNotNull(result.getClient());
        assertEquals(clientId, result.getClient().getClientId());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        assertEquals(LocalDate.parse(accountDTO.getOpeningDate()), result.getOpeningDate());
        assertEquals(new BigDecimal("1000.00"), result.getInitialDeposit());
        assertEquals("SGD", result.getCurrency());
        assertEquals("BR001", result.getBranchId());
    }

    @Test
    void testToDtoList() {
        // Arrange
        List<Account> accounts = Arrays.asList(accountModel);

        // Act
        List<AccountDTO> result = accountMapper.toDtoList(accounts);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        AccountDTO firstResult = result.get(0);
        assertEquals(accountId, firstResult.getAccountId());
        assertEquals(clientId, firstResult.getClientId());
        assertEquals(clientFirstName + " " + clientLastName, firstResult.getClientName());
    }

    @Test
    void testToModelList() {
        // Arrange
        List<AccountDTO> dtos = Arrays.asList(accountDTO);

        // Act
        List<Account> result = accountMapper.toModelList(dtos);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Account firstResult = result.get(0);
        assertEquals(accountId, firstResult.getAccountId());
        assertNotNull(firstResult.getClient());
        assertEquals(clientId, firstResult.getClient().getClientId());
    }

    @Test
    void testToDtoWithNullModel() {
        // Act
        AccountDTO result = accountMapper.toDto(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToModelWithNullDto() {
        // Act
        Account result = accountMapper.toModel(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToDtoWithNullClient() {
        // Arrange
        accountModel.setClient(null);

        // Act
        AccountDTO result = accountMapper.toDto(accountModel);

        // Assert
        assertNotNull(result);
        assertNull(result.getClientId());
        assertEquals("", result.getClientName());
    }
} 