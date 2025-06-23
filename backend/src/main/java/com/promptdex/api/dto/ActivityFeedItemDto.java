package com.promptdex.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * A DTO representing a single item in a user's activity feed.
 * This is a generic wrapper that can contain different types of content.
 *
 * @param eventType A string identifier for the type of event (e.g., "NEW_PROMPT_FROM_FOLLOWING").
 * @param eventTimestamp The timestamp when the event occurred (e.g., the prompt's creation date).
 * @param prompt The prompt data, if the event is prompt-related. Null for other event types.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // This ensures that null fields (like 'prompt') aren't serialized
public record ActivityFeedItemDto(
        String eventType,
        Instant eventTimestamp,
        PromptDto prompt
) {}