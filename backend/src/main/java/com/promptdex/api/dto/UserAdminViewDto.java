package com.promptdex.api.dto;

import com.promptdex.api.model.AuthProvider; // Assuming AuthProvider is in this model package

import java.util.Set;
import java.util.UUID;

public record UserAdminViewDto(
        UUID id,
        String username,
        String email,
        Set<String> roles,
        AuthProvider provider
        // You could add other fields like registrationDate (Instant createdAt) if needed
) {
}