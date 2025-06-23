package com.promptdex.api.service;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private PromptRepository promptRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagService tagService;
    @Mock
    private PromptMapper promptMapper;
    @Mock // Mock UserDetails as it's an interface passed from the controller
    private UserDetails userDetails;

    @InjectMocks
    private PromptService promptService;

    private User author;
    private User otherUser;
    private Prompt prompt;
    private UUID promptId;

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
    }

    @Test
    void createPrompt_shouldSucceed() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.saveAndFlush(any(Prompt.class))).thenReturn(prompt);
        // We can return a mock DTO or null, it doesn't matter for this test's assertions
        when(promptMapper.toDto(any(Prompt.class), any(User.class))).thenReturn(mock(PromptDto.class));


        // Act
        promptService.createPrompt(request, userDetails);

        // Assert
        verify(promptRepository, times(1)).saveAndFlush(any(Prompt.class));
        verify(promptMapper, times(1)).toDto(any(Prompt.class), eq(author));
    }

    @Test
    void updatePrompt_whenUserIsAuthor_shouldSucceed() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(any(Prompt.class))).thenReturn(prompt);

        // Act
        promptService.updatePrompt(promptId, request, userDetails);

        // Assert
        verify(promptRepository, times(1)).save(prompt);
        assertEquals("Updated Title", prompt.getTitle());
    }

    @Test
    void updatePrompt_whenUserIsNotAuthor_shouldThrowAccessDeniedException() {
        // Arrange
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            promptService.updatePrompt(promptId, request, userDetails);
        });

        assertEquals("You do not have permission to edit this prompt.", exception.getMessage());
        verify(promptRepository, never()).save(any());
    }

    @Test
    void deletePrompt_whenUserIsAuthor_shouldSucceed() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

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
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            promptService.deletePrompt(promptId, userDetails);
        });

        verify(promptRepository, never()).delete(any());
    }
}