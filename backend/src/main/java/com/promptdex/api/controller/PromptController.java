package com.promptdex.api.controller;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.service.PromptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID; // Import UUID

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    public ResponseEntity<List<PromptDto>> getAllPrompts() {
        List<PromptDto> prompts = promptService.getAllPrompts();
        return ResponseEntity.ok(prompts);
    }

    // Use UUID in the path variable
    @GetMapping("/{id}")
    public ResponseEntity<PromptDto> getPromptById(@PathVariable UUID id) {
        PromptDto prompt = promptService.getPromptById(id);
        return ResponseEntity.ok(prompt);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> createPrompt(@Valid @RequestBody CreatePromptRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        PromptDto createdPrompt = promptService.createPrompt(request, userDetails.getUsername());
        return new ResponseEntity<>(createdPrompt, HttpStatus.CREATED);
    }

    // Use UUID in the path variable
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> updatePrompt(@PathVariable UUID id, @Valid @RequestBody CreatePromptRequest request, @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        PromptDto updatedPrompt = promptService.updatePrompt(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedPrompt);
    }

    // Use UUID in the path variable
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePrompt(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        promptService.deletePrompt(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}