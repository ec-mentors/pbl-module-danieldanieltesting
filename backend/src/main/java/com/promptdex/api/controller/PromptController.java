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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @AuthenticationPrincipal UserDetails userDetails // Can be null for guests
    ) {
        Page<PromptDto> promptsPage = promptService.searchAndPagePrompts(search, page, size, userDetails);
        return ResponseEntity.ok(promptsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDto> getPromptById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails // Can be null for guests
    ) {
        PromptDto prompt = promptService.getPromptById(id, userDetails);
        return ResponseEntity.ok(prompt);
    }

    // --- NEW BOOKMARK ENDPOINTS ---
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

    // ... (create, update, delete methods remain the same) ...
}