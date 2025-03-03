package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public AccountDTO toDto(Account model) {
        if (model == null) {
            return null;
        }

        return AccountDTO.builder()
                .accountId(model.getAccountId())
                .clientId(model.getClient().getClientId())
                .accountType(model.getAccountType())
                .accountStatus(model.getAccountStatus())
                .openingDate(model.getOpeningDate().format(DATE_FORMATTER))
                .initialDeposit(model.getInitialDeposit())
                .currency(model.getCurrency())
                .branchId(model.getBranchId())
                .build();
    }

    public Account toModel(AccountDTO dto) {
        if (dto == null) {
            return null;
        }

        Account model = new Account();
        model.setAccountId(dto.getAccountId());

        // Note: Client needs to be set separately as we only have clientId in DTO
        Client client = new Client();
        client.setClientId(dto.getClientId());
        model.setClient(client);

        model.setAccountType(dto.getAccountType());
        model.setAccountStatus(dto.getAccountStatus());
        model.setOpeningDate(LocalDate.parse(dto.getOpeningDate(), DATE_FORMATTER));
        model.setInitialDeposit(dto.getInitialDeposit());
        model.setCurrency(dto.getCurrency());
        model.setBranchId(dto.getBranchId());

        return model;
    }

    public List<AccountDTO> toDtoList(List<Account> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(this::toDto)
                .toList();
    }

    public List<Account> toModelList(List<AccountDTO> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toModel)
                .toList();
    }
}