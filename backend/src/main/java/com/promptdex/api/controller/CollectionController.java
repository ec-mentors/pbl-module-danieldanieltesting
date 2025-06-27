package com.promptdex.api.controller;
import com.promptdex.api.dto.CollectionDetailDto;
import com.promptdex.api.dto.CollectionSummaryDto;
import com.promptdex.api.dto.CreateCollectionRequest;
import com.promptdex.api.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/collections")
@PreAuthorize("isAuthenticated()")
public class CollectionController {
    private final CollectionService collectionService;
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    @PostMapping
    public ResponseEntity<CollectionSummaryDto> createCollection(
            @Valid @RequestBody CreateCollectionRequest request,
            @AuthenticationPrincipal UserDetails principal) { 
        CollectionSummaryDto newCollection = collectionService.createCollection(request, principal.getUsername());
        return new ResponseEntity<>(newCollection, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<CollectionSummaryDto>> getMyCollections(
            @AuthenticationPrincipal UserDetails principal) { 
        List<CollectionSummaryDto> collections = collectionService.getCollectionsForUser(principal.getUsername());
        return ResponseEntity.ok(collections);
    }
    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailDto> getCollectionById(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal UserDetails principal) { 
        CollectionDetailDto collection = collectionService.getCollectionById(collectionId, principal.getUsername());
        return ResponseEntity.ok(collection);
    }
    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionSummaryDto> updateCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateCollectionRequest request,
            @AuthenticationPrincipal UserDetails principal) { 
        CollectionSummaryDto updatedCollection = collectionService.updateCollection(collectionId, request, principal.getUsername());
        return ResponseEntity.ok(updatedCollection);
    }
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable UUID collectionId,
            @AuthenticationPrincipal UserDetails principal) { 
        collectionService.deleteCollection(collectionId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{collectionId}/prompts/{promptId}")
    public ResponseEntity<CollectionDetailDto> addPromptToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal UserDetails principal) { 
        CollectionDetailDto updatedCollection = collectionService.addPromptToCollection(collectionId, promptId, principal.getUsername());
        return ResponseEntity.ok(updatedCollection);
    }
    @DeleteMapping("/{collectionId}/prompts/{promptId}")
    public ResponseEntity<Void> removePromptFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID promptId,
            @AuthenticationPrincipal UserDetails principal) { 
        collectionService.removePromptFromCollection(collectionId, promptId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}