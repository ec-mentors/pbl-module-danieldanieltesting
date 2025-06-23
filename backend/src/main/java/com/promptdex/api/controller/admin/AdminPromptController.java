package com.promptdex.api.controller.admin;

import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.service.PromptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault; // Import for default sort
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromptController {

    private final PromptService promptService;

    public AdminPromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    /**
     * GET /api/admin/prompts : Get a paginated list of all prompts, optionally filtered by search term.
     *
     * @param searchTerm Optional term to search by title, text, category, author, tags.
     * @param pageable   Pagination information (e.g., ?page=0&size=10&sort=createdAt,desc).
     * @param principal  The currently authenticated admin user (UserDetails).
     * @return A Page of PromptDto.
     */
    @GetMapping
    public ResponseEntity<Page<PromptDto>> getAllPrompts(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal) {
        Page<PromptDto> prompts = promptService.getAllPromptsAsAdmin(searchTerm, pageable, principal);
        return ResponseEntity.ok(prompts);
    }

    /**
     * DELETE /api/admin/prompts/{promptId} : Delete any prompt by its ID.
     *
     * @param promptId The UUID of the prompt to delete.
     * @return ResponseEntity with status 204 No Content.
     */
    @DeleteMapping("/{promptId}")
    public ResponseEntity<Void> deletePrompt(@PathVariable UUID promptId) {
        promptService.deletePromptAsAdmin(promptId);
        return ResponseEntity.noContent().build();
    }
}