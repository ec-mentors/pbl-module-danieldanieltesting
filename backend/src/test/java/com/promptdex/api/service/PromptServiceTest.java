package com.promptdex.api.service;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
// Import ResourceNotFoundException if you were to assert it for other tests
// import com.promptdex.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; // Import eq for specific argument matching
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagService tagService; // Mocked, though not directly used in these specific fixed tests
    @Mock
    private PromptMapper promptMapper;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PromptService promptService;

    private User author;
    private User otherUser;
    private Prompt prompt;
    private UUID promptId;
    private PromptDto mockPromptDto; // To use for methods returning PromptDto

    @BeforeEach
    void setUp() {
        promptId = UUID.randomUUID();

        author = new User();
        author.setId(UUID.randomUUID());
        author.setUsername("author");

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otherUser");

        prompt = new Prompt();
        prompt.setId(promptId);
        prompt.setAuthor(author);
        prompt.setTitle("Original Title");
        // prompt.setTags(new HashSet<>()); // Initialize if findByIdWithAuthorAndTags interaction is complex

        mockPromptDto = mock(PromptDto.class); // Initialize a generic mock DTO
    }

    @Test
    void createPrompt_shouldSucceed() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        // Assuming saveAndFlush returns the saved prompt itself or a new instance based on it
        when(promptRepository.saveAndFlush(any(Prompt.class))).thenReturn(prompt);
        when(promptMapper.toDto(eq(prompt), eq(author))).thenReturn(mockPromptDto);


        // Act
        PromptDto resultDto = promptService.createPrompt(request, userDetails);

        // Assert
        assertNotNull(resultDto);
        assertSame(mockPromptDto, resultDto);
        verify(promptRepository, times(1)).saveAndFlush(any(Prompt.class));
        verify(promptMapper, times(1)).toDto(eq(prompt), eq(author));
    }

    @Test
    void updatePrompt_whenUserIsAuthor_shouldSucceed() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Updated Text", "Updated Desc", "Updated Model", "Updated Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));

        // FIX: Mock the correct repository method used in PromptService#updatePrompt
        when(promptRepository.findByIdWithAuthorAndTags(promptId)).thenReturn(Optional.of(prompt));

        // Mock the save operation. It might return the same instance or a new one.
        // If it's the same instance being modified and returned:
        when(promptRepository.save(prompt)).thenReturn(prompt);
        // If a new instance could be returned but is equivalent:
        // when(promptRepository.save(any(Prompt.class))).thenReturn(prompt);

        // Mock the mapper call as updatePrompt returns a PromptDto
        when(promptMapper.toDto(eq(prompt), eq(author))).thenReturn(mockPromptDto);

        // Act
        PromptDto resultDto = promptService.updatePrompt(promptId, request, userDetails);

        // Assert
        assertNotNull(resultDto);
        assertSame(mockPromptDto, resultDto, "The DTO returned by the mapper should be returned by the service");
        verify(promptRepository, times(1)).save(prompt); // Verify save was called with the modified prompt
        assertEquals("Updated Title", prompt.getTitle());
        assertEquals("Updated Text", prompt.getPromptText());
        // Add more assertions for other updated fields if necessary
    }

    @Test
    void updatePrompt_whenUserIsNotAuthor_shouldThrowAccessDeniedException() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));

        // FIX: Mock the correct repository method used in PromptService#updatePrompt
        when(promptRepository.findByIdWithAuthorAndTags(promptId)).thenReturn(Optional.of(prompt));
        // Note: prompt is already set up with 'author' as its author in setUp()

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            promptService.updatePrompt(promptId, request, userDetails);
        });

        assertEquals("You do not have permission to edit this prompt.", exception.getMessage());
        verify(promptRepository, never()).save(any());
        verify(promptMapper, never()).toDto(any(), any()); // Mapper should not be called
    }

    @Test
    void deletePrompt_whenUserIsAuthor_shouldSucceed() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        // This mock is correct as PromptService#deletePrompt uses promptRepository.findById(promptId)
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        doNothing().when(promptRepository).delete(prompt); // For void methods

        // Act
        promptService.deletePrompt(promptId, userDetails);

        // Assert
        verify(promptRepository, times(1)).delete(prompt);
    }

    @Test
    void deletePrompt_whenUserIsNotAuthor_shouldThrowAccessDeniedException() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        // This mock is correct as PromptService#deletePrompt uses promptRepository.findById(promptId)
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        // Note: prompt is already set up with 'author' as its author in setUp()

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            promptService.deletePrompt(promptId, userDetails);
        });

        assertEquals("You do not have permission to delete this prompt.", exception.getMessage());
        verify(promptRepository, never()).delete(any());
    }
}