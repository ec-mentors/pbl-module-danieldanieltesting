package com.promptdex.api.controller;
import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.security.UserPrincipal;
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
    public Page<PromptDto> searchPrompts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails principal) { 
        return promptService.searchAndPagePrompts(search, tags, page, size, principal);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PromptDto> getPromptById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) { 
        return ResponseEntity.ok(promptService.getPromptById(id, principal));
    }
    @GetMapping("/user/{username}")
    public Page<PromptDto> getPromptsByAuthor(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails principal) { 
        return promptService.getPromptsByAuthorUsername(username, page, size, principal);
    }
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> createPrompt(
            @Valid @RequestBody CreatePromptRequest request,
            @AuthenticationPrincipal UserDetails principal) { 
        PromptDto createdPrompt = promptService.createPrompt(request, principal);
        return new ResponseEntity<>(createdPrompt, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> updatePrompt(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePromptRequest request,
            @AuthenticationPrincipal UserDetails principal) throws AccessDeniedException { 
        PromptDto updatedPrompt = promptService.updatePrompt(id, request, principal);
        return ResponseEntity.ok(updatedPrompt);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePrompt(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) throws AccessDeniedException { 
        promptService.deletePrompt(id, principal);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromptDto> updatePromptTags(
            @PathVariable("id") UUID promptId,
            @RequestBody Set<String> tagNames,
            @AuthenticationPrincipal UserDetails principal) throws AccessDeniedException { 
        PromptDto updatedPrompt = promptService.updatePromptTags(promptId, tagNames, principal);
        return ResponseEntity.ok(updatedPrompt);
    }
    @PostMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addBookmark(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) { 
        promptService.addBookmark(id, principal.getUsername());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) { 
        promptService.removeBookmark(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public Page<PromptDto> getBookmarkedPrompts(
            @AuthenticationPrincipal UserDetails principal, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return promptService.getBookmarkedPrompts(principal.getUsername(), page, size);
    }
}