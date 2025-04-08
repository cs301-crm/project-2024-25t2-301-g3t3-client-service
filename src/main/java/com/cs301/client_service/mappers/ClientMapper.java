package com.cs301.client_service.mappers;

import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.dtos.ClientListDTO;
import com.cs301.client_service.models.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientMapper {
    private static final Logger logger = LoggerFactory.getLogger(ClientMapper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Converts Client model to ClientDTO
     */
    public ClientDTO toDto(Client model) {
        if (model == null) {
            return null;
        }

        return ClientDTO.builder()
                .clientId(model.getClientId())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .dateOfBirth(model.getDateOfBirth().format(DATE_FORMATTER))
                .gender(model.getGender())
                .emailAddress(model.getEmailAddress())
                .phoneNumber(model.getPhoneNumber())
                .address(model.getAddress())
                .city(model.getCity())
                .state(model.getState())
                .country(model.getCountry())
                .postalCode(model.getPostalCode())
                .nric(model.getNric())
                .agentId(model.getAgentId())
                .verificationStatus(model.getVerificationStatus())
                .build();
    }

    /**
     * Converts ClientDTO to Client model
     */
    public Client toModel(ClientDTO dto) {
        if (dto == null) {
            return null;
        }

        Client model = new Client();
        model.setClientId(dto.getClientId());
        model.setFirstName(dto.getFirstName());
        model.setLastName(dto.getLastName());
        model.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth(), DATE_FORMATTER));
        model.setGender(dto.getGender());
        model.setEmailAddress(dto.getEmailAddress());
        model.setPhoneNumber(dto.getPhoneNumber());
        model.setAddress(dto.getAddress());
        model.setCity(dto.getCity());
        model.setState(dto.getState());
        model.setCountry(dto.getCountry());
        model.setPostalCode(dto.getPostalCode());
        model.setNric(dto.getNric());
        model.setAgentId(dto.getAgentId());
        
        // Set default verification status if not provided
        if (dto.getVerificationStatus() == null) {
            model.setVerificationStatus(VerificationStatus.PENDING);
        } else {
            model.setVerificationStatus(dto.getVerificationStatus());
        }

        return model;
    }
    
    /**
     * Converts Client model to simplified ClientListDTO
     */
    public ClientListDTO toListDto(Client model) {
        if (model == null) {
            return null;
        }
        
        return ClientListDTO.builder()
                .clientId(model.getClientId())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .build();
    }
    
    /**
     * Converts a list of Client models to a list of ClientListDTOs
     */
    public List<ClientListDTO> toListDtoList(List<Client> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        
        return models.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }
}
