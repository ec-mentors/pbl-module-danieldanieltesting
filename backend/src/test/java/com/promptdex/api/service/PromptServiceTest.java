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
import static org.mockito.ArgumentMatchers.eq; 
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
    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private PromptService promptService;
    private User author;
    private User otherUser;
    private Prompt prompt;
    private UUID promptId;
    private PromptDto mockPromptDto; 
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
        mockPromptDto = mock(PromptDto.class); 
    }
    @Test
    void createPrompt_shouldSucceed() {
        CreatePromptRequest request = new CreatePromptRequest("Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.saveAndFlush(any(Prompt.class))).thenReturn(prompt);
        when(promptMapper.toDto(eq(prompt), eq(author))).thenReturn(mockPromptDto);
        PromptDto resultDto = promptService.createPrompt(request, userDetails);
        assertNotNull(resultDto);
        assertSame(mockPromptDto, resultDto);
        verify(promptRepository, times(1)).saveAndFlush(any(Prompt.class));
        verify(promptMapper, times(1)).toDto(eq(prompt), eq(author));
    }
    @Test
    void updatePrompt_whenUserIsAuthor_shouldSucceed() {
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Updated Text", "Updated Desc", "Updated Model", "Updated Category");
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.findByIdWithAuthorAndTags(promptId)).thenReturn(Optional.of(prompt));
        when(promptRepository.save(prompt)).thenReturn(prompt);
        when(promptMapper.toDto(eq(prompt), eq(author))).thenReturn(mockPromptDto);
        PromptDto resultDto = promptService.updatePrompt(promptId, request, userDetails);
        assertNotNull(resultDto);
        assertSame(mockPromptDto, resultDto, "The DTO returned by the mapper should be returned by the service");
        verify(promptRepository, times(1)).save(prompt); 
        assertEquals("Updated Title", prompt.getTitle());
        assertEquals("Updated Text", prompt.getPromptText());
    }
    @Test
    void updatePrompt_whenUserIsNotAuthor_shouldThrowAccessDeniedException() {
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Text", "Desc", "Model", "Category");
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(promptRepository.findByIdWithAuthorAndTags(promptId)).thenReturn(Optional.of(prompt));
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            promptService.updatePrompt(promptId, request, userDetails);
        });
        assertEquals("You do not have permission to edit this prompt.", exception.getMessage());
        verify(promptRepository, never()).save(any());
        verify(promptMapper, never()).toDto(any(), any()); 
    }
    @Test
    void deletePrompt_whenUserIsAuthor_shouldSucceed() {
        when(userDetails.getUsername()).thenReturn("author");
        when(userRepository.findByUsername("author")).thenReturn(Optional.of(author));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        doNothing().when(promptRepository).delete(prompt); 
        promptService.deletePrompt(promptId, userDetails);
        verify(promptRepository, times(1)).delete(prompt);
    }
    @Test
    void deletePrompt_whenUserIsNotAuthor_shouldThrowAccessDeniedException() {
        when(userDetails.getUsername()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(promptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            promptService.deletePrompt(promptId, userDetails);
        });
        assertEquals("You do not have permission to delete this prompt.", exception.getMessage());
        verify(promptRepository, never()).delete(any());
    }
}