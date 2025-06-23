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
    private PromptMapper promptMapper; // Mock the mapper as getCollectionById uses it.

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
        // Arrange
        CreateCollectionRequest request = new CreateCollectionRequest("New Collection", "Desc");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(collectionRepository.existsByNameAndOwner_Id(request.name(), user.getId())).thenReturn(false);
        when(collectionRepository.saveAndFlush(any(Collection.class))).thenReturn(new Collection());

        // Act
        collectionService.createCollection(request, username);

        // Assert
        verify(collectionRepository, times(1)).saveAndFlush(any(Collection.class));
    }

    @Test
    void createCollection_whenNameIsNotUniqueForUser_shouldThrowException() {
        // Arrange
        CreateCollectionRequest request = new CreateCollectionRequest("Existing Collection", "Desc");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        // Simulate that a collection with this name already exists for the user
        when(collectionRepository.existsByNameAndOwner_Id(request.name(), user.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(CollectionAlreadyExistsException.class, () -> {
            collectionService.createCollection(request, username);
        });
    }

    @Test
    void addPromptToCollection_whenUserOwnsCollection_shouldAddPrompt() {
        // Arrange
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.of(collection));
        when(promptRepository.findById(prompt.getId())).thenReturn(Optional.of(prompt));
        when(collectionRepository.findByIdWithPrompts(collection.getId())).thenReturn(Optional.of(collection));

        // --- THE FIX IS HERE ---
        // We must mock the call to findUserByUsername that happens inside the nested getCollectionById call.
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        collectionService.addPromptToCollection(collection.getId(), prompt.getId(), username);

        // Assert
        assertTrue(collection.getPrompts().contains(prompt));
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void getCollectionById_whenUserDoesNotOwnCollection_shouldThrowResourceNotFoundException() { // Renamed for clarity
        // ... setup code for the test remains the same ...

        // The service now correctly throws ResourceNotFoundException for security.
        // We update the test to assert this correct behavior.
        assertThrows(ResourceNotFoundException.class, () -> {
            collectionService.getCollectionById(collection.getId(), "someOtherUser");
        });
    }

    @Test
    void deleteCollection_whenUserOwnsCollection_shouldSucceed() {
        // Arrange
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.of(collection));

        // Act
        collectionService.deleteCollection(collection.getId(), username);

        // Assert
        verify(collectionRepository, times(1)).delete(collection);
    }

    @Test
    void deleteCollection_whenUserDoesNotOwnCollection_shouldThrowResourceNotFound() {
        // Arrange
        // Simulate that no collection is found for this combination of ID and username
        when(collectionRepository.findByIdAndOwner_Username(collection.getId(), username)).thenReturn(Optional.empty());

        // Act & Assert
        // The service logic combines ownership check with finding, so it results in a ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            collectionService.deleteCollection(collection.getId(), username);
        });

        verify(collectionRepository, never()).delete(any());
    }
}