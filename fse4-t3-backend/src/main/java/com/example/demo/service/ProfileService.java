package com.example.demo.service;

import com.example.demo.dto.identification.ClientIdentificationPutDto;
import com.example.demo.dto.profile.ProfileDto;
import com.example.demo.dto.profile.UpdateProfileDto;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.models.Client;
import com.example.demo.models.ClientIdentification;
import com.example.demo.repository.ClientIdentificationRepository;
import com.example.demo.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ClientRepository clientRepository;
    private final ClientIdentificationRepository clientIdentificationRepository;

    public ProfileService(ClientRepository clientRepository, ClientIdentificationRepository clientIdentificationRepository) {
        this.clientRepository = clientRepository;
        this.clientIdentificationRepository = clientIdentificationRepository;
    }

    public ProfileDto getProfile(String clientId) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new ResourceNotFoundException("Client not found");
        }
        Client client = clientOpt.get();
        Set<ClientIdentification> identifications = clientIdentificationRepository.findByClientId(clientId);
        return mapToProfileDto(client, identifications);
    }

    @Transactional
    public ProfileDto updateProfile(String clientId, UpdateProfileDto updateDto) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new ResourceNotFoundException("Client not found");
        }
        Client client = clientOpt.get();
        boolean updated = false;
        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            client.setName(updateDto.getName());
            updated = true;
        }
        if (updateDto.getCountry() != null && !updateDto.getCountry().isBlank()) {
            client.setCountry(updateDto.getCountry());
            updated = true;
        }
        if (updateDto.getPostalCode() != null && !updateDto.getPostalCode().isBlank()) {
            client.setPostalCode(updateDto.getPostalCode());
            updated = true;
        }
        if (updated) {
            clientRepository.update(client);
        }
        return getProfile(clientId);
    }

    @Transactional
    public ProfileDto updateIdentifications(String clientId, List<ClientIdentificationPutDto> updatedIdentificationsDto) {
        if (clientRepository.findById(clientId).isEmpty()) {
            throw new ResourceNotFoundException("Client not found");
        }
        validateIdentificationUniqueness(clientId, updatedIdentificationsDto);
        for (ClientIdentificationPutDto dto : updatedIdentificationsDto) {
            Optional<ClientIdentification> existingIdOpt = clientIdentificationRepository.findByClientIdAndType(clientId, dto.getType());
            if (existingIdOpt.isPresent()) {
                ClientIdentification existingId = existingIdOpt.get();
                if (!existingId.getValue().equals(dto.getValue())) {
                    clientIdentificationRepository.updateValue(existingId.getId(), dto.getValue().trim());
                }
            } else {
                ClientIdentification newId = new ClientIdentification();
                newId.setId(UUID.randomUUID().toString());
                newId.setClientId(clientId);
                newId.setType(dto.getType());
                newId.setValue(dto.getValue());
                clientIdentificationRepository.insert(newId);
            }
        }
        return getProfile(clientId);
    }

    private void validateIdentificationUniqueness(String currentClientId, List<ClientIdentificationPutDto> identifications) {
        if (identifications == null || identifications.isEmpty()) {
            return;
        }
        long distinctTypeCount = identifications.stream()
                .map(ClientIdentificationPutDto::getType)
                .distinct()
                .count();
        if (distinctTypeCount < identifications.size()) {
            throw new InvalidRequestException("Duplicate identification types are not allowed in the same request.");
        }
        List<String> values = identifications.stream().map(ClientIdentificationPutDto::getValue).collect(Collectors.toList());
        long distinctValueCount = values.stream().distinct().count();
        if (distinctValueCount < values.size()) {
            throw new InvalidRequestException("Duplicate identification values are not allowed in the same request.");
        }
        List<ClientIdentification> existingIdentifications = clientIdentificationRepository.findByValueIn(values);
        boolean existsElsewhere = existingIdentifications.stream()
                .anyMatch(existingId -> !existingId.getClientId().equals(currentClientId));
        if (existsElsewhere) {
            throw new InvalidRequestException("One or more identification values are already in use by another client.");
        }
    }

    public ProfileDto mapToProfileDto(Client client, Set<ClientIdentification> identifications) {
    ProfileDto dto = new ProfileDto();
    dto.setClientId(client.getClientId());
    dto.setName(client.getName());
    dto.setEmail(client.getEmail());
    dto.setDateOfBirth(client.getDateOfBirth());
    dto.setCountry(client.getCountry());
    dto.setPostalCode(client.getPostalCode());
    dto.setIdentification(identifications);
    return dto;
    }
}
