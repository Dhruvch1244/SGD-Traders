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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientIdentificationRepository clientIdentificationRepository;
    @InjectMocks
    private ProfileService service;

    private Client testClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testClient = new Client();
        testClient.setClientId("client1");
        testClient.setName("John Doe");
        testClient.setEmail("john.doe@example.com");
        testClient.setCountry("USA");
        testClient.setPostalCode("12345");
    }

    // --- getProfile Tests ---

    @Test
    @DisplayName("getProfile throws ResourceNotFoundException for unknown clientId")
    void testGetProfile_throwsResourceNotFoundException_whenClientNotFound() {
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProfile("unknown"));
    }

    @Test
    @DisplayName("getProfile returns correct ProfileDto on happy path")
    void testGetProfile_returnsProfileDto_happyPath() {
        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByClientId("client1")).thenReturn(Collections.emptySet());

        ProfileDto result = service.getProfile("client1");

        assertNotNull(result);
        assertEquals("client1", result.getClientId());
        assertEquals("John Doe", result.getName());
    }

    // --- updateProfile Tests ---

    @Test
    @DisplayName("updateProfile throws ResourceNotFoundException for unknown clientId")
    void testUpdateProfile_throwsResourceNotFoundException_whenClientNotFound() {
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setName("Jane Doe");
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateProfile("unknown", dto));
    }

    @Test
    @DisplayName("updateProfile updates client data correctly")
    void testUpdateProfile_updatesClient_happyPath() {
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setName("Jane Doe");
        dto.setCountry("Canada");
        dto.setPostalCode("A1A 1A1");

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByClientId("client1")).thenReturn(Collections.emptySet());

        ProfileDto result = service.updateProfile("client1", dto);

        verify(clientRepository).update(any(Client.class));
        assertEquals("Jane Doe", result.getName());
        assertEquals("Canada", result.getCountry());
        assertEquals("A1A 1A1", result.getPostalCode());
    }

    @Test
    @DisplayName("updateProfile does not update with blank values")
    void testUpdateProfile_doesNotUpdateWithBlankValues() {
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setName(" ");
        dto.setCountry("");

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByClientId("client1")).thenReturn(Collections.emptySet());

        service.updateProfile("client1", dto);

        verify(clientRepository, never()).update(any(Client.class));
    }

    // --- updateIdentifications Tests ---

    @Test
    @DisplayName("updateIdentifications throws ResourceNotFoundException for unknown clientId")
    void testUpdateIdentifications_throwsResourceNotFoundException_whenClientNotFound() {
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateIdentifications("unknown", Collections.emptyList()));
    }

    @Test
    @DisplayName("updateIdentifications adds a new identification")
    void testUpdateIdentifications_addsNewIdentification() {
        ClientIdentificationPutDto dto = new ClientIdentificationPutDto("PASSPORT", "12345");
        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByClientIdAndType("client1", "PASSPORT")).thenReturn(Optional.empty());

        service.updateIdentifications("client1", Collections.singletonList(dto));

        verify(clientIdentificationRepository).insert(any(ClientIdentification.class));
    }

    @Test
    @DisplayName("updateIdentifications updates an existing identification")
    void testUpdateIdentifications_updatesExistingIdentification() {
        ClientIdentificationPutDto dto = new ClientIdentificationPutDto("PASSPORT", "54321");
        ClientIdentification existingId = new ClientIdentification(UUID.randomUUID().toString(), "client1", "PASSPORT", "12345",true);

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByClientIdAndType("client1", "PASSPORT")).thenReturn(Optional.of(existingId));

        service.updateIdentifications("client1", Collections.singletonList(dto));

        verify(clientIdentificationRepository).updateValue(existingId.getId(), "54321");
        verify(clientIdentificationRepository, never()).insert(any(ClientIdentification.class));
    }

    @Test
    @DisplayName("updateIdentifications throws InvalidRequestException for duplicate types in request")
    void testUpdateIdentifications_throwsInvalidRequest_forDuplicateTypes() {
        ClientIdentificationPutDto dto1 = new ClientIdentificationPutDto("PASSPORT", "123");
        ClientIdentificationPutDto dto2 = new ClientIdentificationPutDto("PASSPORT", "456");

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));

        Exception ex = assertThrows(InvalidRequestException.class, () -> service.updateIdentifications("client1", Arrays.asList(dto1, dto2)));
        assertTrue(ex.getMessage().contains("Duplicate identification types"));
    }

    @Test
    @DisplayName("updateIdentifications throws InvalidRequestException for duplicate values in request")
    void testUpdateIdentifications_throwsInvalidRequest_forDuplicateValues() {
        ClientIdentificationPutDto dto1 = new ClientIdentificationPutDto("PASSPORT", "123");
        ClientIdentificationPutDto dto2 = new ClientIdentificationPutDto("DRIVERS_LICENSE", "123");

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));

        Exception ex = assertThrows(InvalidRequestException.class, () -> service.updateIdentifications("client1", Arrays.asList(dto1, dto2)));
        assertTrue(ex.getMessage().contains("Duplicate identification values"));
    }

    @Test
    @DisplayName("updateIdentifications throws InvalidRequestException for value used by another client")
    void testUpdateIdentifications_throwsInvalidRequest_forValueUsedByAnotherClient() {
        ClientIdentificationPutDto dto = new ClientIdentificationPutDto("PASSPORT", "123");
        ClientIdentification existingId = new ClientIdentification(UUID.randomUUID().toString(), "client2", "PASSPORT", "123",true);

        when(clientRepository.findById("client1")).thenReturn(Optional.of(testClient));
        when(clientIdentificationRepository.findByValueIn(Collections.singletonList("123"))).thenReturn(Collections.singletonList(existingId));

        Exception ex = assertThrows(InvalidRequestException.class, () -> service.updateIdentifications("client1", Collections.singletonList(dto)));
        assertTrue(ex.getMessage().contains("already in use by another client"));
    }

    // --- mapToProfileDto Test ---
    @Test
    @DisplayName("mapToProfileDto maps data correctly")
    void testMapToProfileDto() {
        ClientIdentification id = new ClientIdentification(UUID.randomUUID().toString(), "client1", "PASSPORT", "123",true);
        Set<ClientIdentification> identifications = Set.of(id);

        ProfileDto dto = service.mapToProfileDto(testClient, identifications);

        assertEquals("client1", dto.getClientId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals(1, dto.getIdentification().size());
    }
}
