package com.promptdex.api.controller;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.service.PromptService;
import com.promptdex.api.service.UserService;
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
    private final UserService userService;

    public UserController(PromptService promptService, UserService userService) {
        this.promptService = promptService;
        this.userService = userService;
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<ProfileDto> getUserProfile(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails principal) { // <-- FIX: Changed from UserPrincipal to UserDetails
        ProfileDto profile = userService.getProfile(username, principal);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{username}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileDto> followUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails principal) { // <-- FIX: Changed from UserPrincipal to UserDetails
        ProfileDto profile = userService.followUser(username, principal);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{username}/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileDto> unfollowUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails principal) { // <-- FIX: Changed from UserPrincipal to UserDetails
        ProfileDto profile = userService.unfollowUser(username, principal);
        return ResponseEntity.ok(profile);
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