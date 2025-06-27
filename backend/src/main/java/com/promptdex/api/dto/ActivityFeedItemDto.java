package com.promptdex.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivityFeedItemDto(
        String eventType,
        Instant eventTimestamp,
        PromptDto prompt
) {
}