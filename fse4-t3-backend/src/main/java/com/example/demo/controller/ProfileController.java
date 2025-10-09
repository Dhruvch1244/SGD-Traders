package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.identification.ClientIdentificationPutDto;
import com.example.demo.dto.profile.ProfileDto;
import com.example.demo.dto.profile.UpdateProfileDto;
import com.example.demo.service.ProfileService;
import com.example.demo.exception.InvalidRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable String clientId) {
        try {
            ProfileDto profile = profileService.getProfile(clientId);
            return ResponseEntity.ok(profile);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ApiResponse> updateProfile(@PathVariable String clientId, @RequestBody UpdateProfileDto updateDto) {
        try {
            ProfileDto updatedProfile = profileService.updateProfile(clientId, updateDto);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedProfile));
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse(false, "Profile not found"), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{clientId}/identification")
    public ResponseEntity<ApiResponse> updateIdentifications(@PathVariable String clientId, @RequestBody List<ClientIdentificationPutDto> identifications) {
        try {
            ProfileDto updatedProfile = profileService.updateIdentifications(clientId, identifications);
            return ResponseEntity.ok(new ApiResponse(true, "Identifications updated successfully", updatedProfile));
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse(false, "Profile not found"), HttpStatus.NOT_FOUND);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
