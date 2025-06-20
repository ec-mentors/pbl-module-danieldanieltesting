// src/main/java/com/promptdex/api/controller/CollectionController.java
package com.promptdex.api.controller;

import com.promptdex.api.dto.CollectionDetailDto;
import com.promptdex.api.dto.CollectionSummaryDto;
import com.promptdex.api.dto.CreateCollectionRequest;
import com.promptdex.api.security.UserPrincipal;
import com.promptdex.api.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
@PreAuthorize("isAuthenticated()") // All methods in this controller require authentication
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<CollectionSummaryDto> createCollection(
            @Valid @RequestBody CreateCollectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CollectionSummaryDto newCollection = collectionService.createCollection(request, principal.getUsername());
        return new ResponseEntity<>(newCollection, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CollectionSummaryDto>> getMyCollections(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CollectionSummaryDto> collections = collectionService.getCollectionsForUser(principal.getUsername());
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailDto> getCollectionById(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CollectionDetailDto collection = collectionService.getCollectionById(collectionId, principal.getUsername());
        return ResponseEntity.ok(collection);
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionSummaryDto> updateCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateCollectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CollectionSummaryDto updatedCollection = collectionService.updateCollection(collectionId, request, principal.getUsername());
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        collectionService.deleteCollection(collectionId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{collectionId}/prompts/{promptId}")
    public ResponseEntity<Void> addPromptToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal UserPrincipal principal) {
        collectionService.addPromptToCollection(collectionId, promptId, principal.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{collectionId}/prompts/{promptId}")
    public ResponseEntity<Void> removePromptFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal UserPrincipal principal) {
        collectionService.removePromptFromCollection(collectionId, promptId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}