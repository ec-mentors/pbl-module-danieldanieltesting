package com.promptdex.api.dto;

import jakarta.validation.constraints.NotEmpty; // For validation
import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty // Ensures the set of roles is not empty
        Set<String> roles
) {
}