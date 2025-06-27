package com.promptdex.api.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PromptRepository promptRepository;
    @Mock
    private PromptMapper promptMapper;
    @InjectMocks
    private CollectionService collectionService;
    private User user;
    private Collection collection;
    private Prompt prompt;
    private String username = "testUser";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        collection = new Collection("My Collection", "A test collection", user);
        collection.setId(UUID.randomUUID());
        prompt = new Prompt();
        prompt.setId(UUID.randomUUID());
    }

    @Test
    void createCollection_whenNameIsUniqueForUser_shouldSucceed() {
        CreateCollectionRequest request = new CreateCollectionRequest("New Collection", "Desc");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(collectionRepository.existsByNameAndOwner_Id(request.name(), user.getId())).thenReturn(false);
        when(collectionRepository.saveAndFlush(any(Collection.class))).thenReturn(new Collection());
        collectionService.createCollection(request, username);
        verify(collectionRepository, times(1)).saveAndFlush(any(Collection.class));
    }

    @Test
    void createCollection_whenNameIsNotUniqueForUser_shouldThrowException() {
        CreateCollectionRequest request = new CreateCollectionRequest("Existing Collection", "Desc");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(collectionRepository.existsByNameAndOwner_Id(request.name(), user.getId())).thenReturn(true);
        assertThrows(CollectionAlreadyExistsException.class, () -> {
            collectionService.createCollection(request, username);
        });
    }

    @Test
    void addPromptToCollection_whenUserOwnsCollection_shouldAddPrompt() {
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.of(collection));
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(collectionRepository.findByIdWithPrompts(collection.getId())).thenReturn(Optional.of(collection));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        collectionService.addPromptToCollection(collection.getId(), prompt.getId(), username);
        assertTrue(collection.getPrompts().contains(prompt));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void getCollectionById_whenUserDoesNotOwnCollection_shouldThrowResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            collectionService.getCollectionById(collection.getId(), "someOtherUser");
        });
    }

    @Test
    void deleteCollection_whenUserOwnsCollection_shouldSucceed() {
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.of(collection));
        collectionService.deleteCollection(collection.getId(), username);
        verify(collectionRepository, times(1)).delete(collection);
    }

    @Test
    void deleteCollection_whenUserDoesNotOwnCollection_shouldThrowResourceNotFound() {
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            collectionService.deleteCollection(collection.getId(), username);
        });
        verify(collectionRepository, never()).delete(any());
    }
}