package com.promptdex.api.service;

import com.promptdex.api.dto.ActivityFeedItemDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PromptRepository promptRepository;
    @Mock
    private PromptMapper promptMapper;
    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private FeedService feedService;
    private User currentUser;
    private User followedUser1;
    private User followedUser2;

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
        Set<User> followingSet = new LinkedHashSet<>();
        followingSet.add(followedUser1);
        followingSet.add(followedUser2);
        currentUser.setFollowing(followingSet);
    }

    @Test
    void getFeedForUser_whenFollowingUsers_shouldReturnPageOfPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userDetails.getUsername()).thenReturn(currentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        List<UUID> followedUserIds = List.of(followedUser1.getId(), followedUser2.getId());
        List<Prompt> prompts = List.of(new Prompt(), new Prompt());
        Page<Prompt> promptPage = new PageImpl<>(prompts, pageable, prompts.size());
        when(promptRepository.findByAuthor_IdInOrderByCreatedAtDesc(followedUserIds, pageable)).thenReturn(promptPage);
        PromptDto mockPromptDto = mock(PromptDto.class);
        when(promptMapper.toDto(any(Prompt.class), any(User.class))).thenReturn(mockPromptDto);
        Page<ActivityFeedItemDto> result = feedService.getFeedForUser(userDetails, 0, 10);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void getFeedForUser_whenFollowingNoOne_shouldReturnEmptyPage() {
        currentUser.setFollowing(new HashSet<>());
        when(userDetails.getUsername()).thenReturn(currentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing(currentUser.getUsername())).thenReturn(Optional.of(currentUser));
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityFeedItemDto> result = feedService.getFeedForUser(userDetails, 0, 10);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}