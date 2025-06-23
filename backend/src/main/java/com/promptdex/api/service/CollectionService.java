package com.promptdex.api.service;

import com.promptdex.api.dto.CollectionDetailDto;
import com.promptdex.api.dto.CollectionSummaryDto;
import com.promptdex.api.dto.CreateCollectionRequest;
import com.promptdex.api.exception.CollectionAlreadyExistsException;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Collection;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.CollectionRepository;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException; // No longer used in getCollectionById, but kept for other potential uses
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final PromptRepository promptRepository;
    private final PromptMapper promptMapper;

    public CollectionService(CollectionRepository collectionRepository, UserRepository userRepository, PromptRepository promptRepository, PromptMapper promptMapper) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.promptMapper = promptMapper;
    }

    public List<CollectionSummaryDto> getCollectionsForUser(String username) {
        User user = findUserByUsername(username);
        List<Collection> collections = collectionRepository.findByOwner_UsernameOrderByNameAsc(user.getUsername());
        return collections.stream()
                .map(collection -> new CollectionSummaryDto(
                        collection.getId(),
                        collection.getName(),
                        collection.getDescription(),
                        collection.getPrompts().size(),
                        collection.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public CollectionDetailDto getCollectionById(UUID collectionId, String username) {
        User user = findUserByUsername(username);
        Collection collection = collectionRepository.findByIdWithPrompts(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId));

        // --- THIS IS THE FIX ---
        // Instead of throwing AccessDeniedException (403), we throw ResourceNotFoundException (404).
        // This is a more secure practice as it doesn't reveal the existence of a resource
        // to unauthorized users. It makes the endpoint's behavior consistent.
        if (!collection.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Collection not found with id: " + collectionId);
        }

        List<com.promptdex.api.dto.PromptDto> promptDtos = collection.getPrompts().stream()
                .map(prompt -> promptMapper.toDto(prompt, user))
                .sorted(Comparator.comparing(com.promptdex.api.dto.PromptDto::createdAt).reversed())
                .collect(Collectors.toList());

        return new CollectionDetailDto(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getCreatedAt(),
                collection.getUpdatedAt(),
                promptDtos
        );
    }

    public CollectionSummaryDto createCollection(CreateCollectionRequest request, String username) {
        User user = findUserByUsername(username);
        if (collectionRepository.existsByNameAndOwner_Id(request.name(), user.getId())) {
            throw new CollectionAlreadyExistsException("A collection with the name '" + request.name() + "' already exists.");
        }
        Collection newCollection = new Collection(request.name(), request.description(), user);

        Collection savedCollection = collectionRepository.saveAndFlush(newCollection);

        return new CollectionSummaryDto(
                savedCollection.getId(),
                savedCollection.getName(),
                savedCollection.getDescription(),
                0, // Starts with 0 prompts
                savedCollection.getCreatedAt()
        );
    }

    public CollectionDetailDto addPromptToCollection(UUID collectionId, UUID promptId, String username) {
        Collection collection = findCollectionByIdAndOwner(collectionId, username);
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        collection.getPrompts().add(prompt);
        collectionRepository.save(collection);
        return getCollectionById(collectionId, username);
    }

    public void removePromptFromCollection(UUID collectionId, UUID promptId, String username) {
        Collection collection = findCollectionByIdAndOwner(collectionId, username);
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));

        collection.getPrompts().remove(prompt);
        collectionRepository.save(collection);
    }

    public CollectionSummaryDto updateCollection(UUID collectionId, CreateCollectionRequest request, String username) {
        Collection collection = findCollectionByIdAndOwner(collectionId, username);

        if (!collection.getName().equalsIgnoreCase(request.name()) &&
                collectionRepository.existsByNameAndOwner_Id(request.name(), collection.getOwner().getId())) {
            throw new CollectionAlreadyExistsException("A collection with the name '" + request.name() + "' already exists.");
        }

        collection.setName(request.name());
        collection.setDescription(request.description());
        Collection updatedCollection = collectionRepository.save(collection);

        return new CollectionSummaryDto(
                updatedCollection.getId(),
                updatedCollection.getName(),
                updatedCollection.getDescription(),
                updatedCollection.getPrompts().size(),
                updatedCollection.getCreatedAt()
        );
    }

    public void deleteCollection(UUID collectionId, String username) {
        Collection collection = findCollectionByIdAndOwner(collectionId, username);
        collectionRepository.delete(collection);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private Collection findCollectionByIdAndOwner(UUID collectionId, String username) {
        return collectionRepository.findByIdAndOwner_Username(collectionId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found with id: " + collectionId + " for user " + username));
    }
}