// src/main/java/com/promptdex/api/dto/CollectionSummaryDto.java
package com.promptdex.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CollectionSummaryDto(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("promptCount")
        int promptCount,

        @JsonProperty("createdAt")
        Instant createdAt
) {}