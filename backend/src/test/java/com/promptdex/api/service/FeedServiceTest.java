package com.promptdex.api.service;

import com.promptdex.api.dto.ActivityFeedItemDto;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PromptRepository promptRepository;
    @Mock
    private PromptMapper promptMapper;

    @InjectMocks
    private FeedService feedService;

    private User currentUser;
    private User followedUser1;
    private User followedUser2;
    private UserPrincipal currentUserPrincipal;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setUsername("currentUser");

        followedUser1 = new User();
        followedUser1.setId(UUID.randomUUID());
        followedUser1.setUsername("followedUser1");

        followedUser2 = new User();
        followedUser2.setId(UUID.randomUUID());
        followedUser2.setUsername("followedUser2");

        currentUser.setFollowing(Set.of(followedUser1, followedUser2));
        currentUserPrincipal = new UserPrincipal(currentUser);
    }

    @Test
    void getFeedForUser_whenFollowingUsers_shouldReturnPageOfPrompts() {
        // Arrange
        List<UUID> followedIds = List.of(followedUser1.getId(), followedUser2.getId());
        Pageable pageable = PageRequest.of(0, 10);

        // Create some mock prompts from the followed users
        Prompt prompt1 = new Prompt();
        prompt1.setCreatedAt(Instant.now());
        Prompt prompt2 = new Prompt();
        prompt2.setCreatedAt(Instant.now().minusSeconds(100));

        List<Prompt> mockPrompts = List.of(prompt1, prompt2);
        Page<Prompt> promptPage = new PageImpl<>(mockPrompts, pageable, mockPrompts.size());

        // Mock DTOs to be returned by the mapper
        PromptDto dto1 = new PromptDto(UUID.randomUUID(), "Title1", "", "", "", "", "", Instant.now(), Instant.now(), 0, List.of(), List.of(), false);
        PromptDto dto2 = new PromptDto(UUID.randomUUID(), "Title2", "", "", "", "", "", Instant.now(), Instant.now(), 0, List.of(), List.of(), false);

        when(userRepository.findByUsernameWithFollowing("currentUser")).thenReturn(Optional.of(currentUser));
        when(promptRepository.findByAuthor_IdInOrderByCreatedAtDesc(followedIds, pageable)).thenReturn(promptPage);
        // Configure the mapper to return a DTO when called with the corresponding prompt
        when(promptMapper.toDto(prompt1, currentUser)).thenReturn(dto1);
        when(promptMapper.toDto(prompt2, currentUser)).thenReturn(dto2);

        // Act
        Page<ActivityFeedItemDto> resultPage = feedService.getFeedForUser(currentUserPrincipal, 0, 10);

        // Assert
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Title1", resultPage.getContent().get(0).prompt().title());
        assertEquals("Title2", resultPage.getContent().get(1).prompt().title());
        verify(promptRepository, times(1)).findByAuthor_IdInOrderByCreatedAtDesc(anyList(), any(Pageable.class));
    }

    @Test
    void getFeedForUser_whenNotFollowingAnyone_shouldReturnEmptyPage() {
        // Arrange
        currentUser.setFollowing(Set.of()); // User follows no one
        when(userRepository.findByUsernameWithFollowing("currentUser")).thenReturn(Optional.of(currentUser));

        // Act
        Page<ActivityFeedItemDto> resultPage = feedService.getFeedForUser(currentUserPrincipal, 0, 10);

        // Assert
        assertTrue(resultPage.isEmpty());
        // Verify the prompt repository was never queried
        verify(promptRepository, never()).findByAuthor_IdInOrderByCreatedAtDesc(any(), any());
    }
}