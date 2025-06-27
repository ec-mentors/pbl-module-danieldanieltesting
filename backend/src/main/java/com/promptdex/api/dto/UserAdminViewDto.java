package com.promptdex.api.dto;
import com.promptdex.api.model.AuthProvider; 
import java.util.Set;
import java.util.UUID;
public record UserAdminViewDto(
        UUID id,
        String username,
        String email,
        Set<String> roles,
        AuthProvider provider
) {
}