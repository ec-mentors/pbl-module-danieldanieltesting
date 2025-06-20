package com.promptdex.api.controller;

import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.service.PromptService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final PromptService promptService;

    public UserController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping("/{username}/prompts")
    public ResponseEntity<Page<PromptDto>> getPromptsByUser(
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<PromptDto> promptsPage = promptService.getPromptsByAuthorUsername(username, page, size, userDetails);
        return ResponseEntity.ok(promptsPage);
    }

    // --- NEW ENDPOINT FOR "MY BOOKMARKS" ---
    @GetMapping("/me/bookmarks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PromptDto>> getMyBookmarkedPrompts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<PromptDto> promptsPage = promptService.getBookmarkedPrompts(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(promptsPage);
    }
}