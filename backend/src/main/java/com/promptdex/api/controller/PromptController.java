package com.promptdex.api.controller;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.service.PromptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    public ResponseEntity<Page<PromptDto>> getAllPrompts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<PromptDto> promptsPage = promptService.searchAndPagePrompts(search, tags, page, size, userDetails);
        return ResponseEntity.ok(promptsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDto> getPromptById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PromptDto prompt = promptService.getPromptById(id, userDetails);
        return ResponseEntity.ok(prompt);
    }

    @PostMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addBookmark(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        promptService.addBookmark(id, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        promptService.removeBookmark(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> updatePromptTags(
            @PathVariable UUID id,
            @RequestBody Set<String> tagNames,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        PromptDto updatedPrompt = promptService.updatePromptTags(id, tagNames, userDetails.getUsername());
        return ResponseEntity.ok(updatedPrompt);
    }

    // --- FIX: RESTORED CREATE, UPDATE, AND DELETE ENDPOINTS ---

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> createPrompt(
            @Valid @RequestBody CreatePromptRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PromptDto newPrompt = promptService.createPrompt(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(newPrompt);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> updatePrompt(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePromptRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        PromptDto updatedPrompt = promptService.updatePrompt(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedPrompt);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        promptService.deletePrompt(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}