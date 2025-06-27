package com.promptdex.api.service;
import com.promptdex.api.dto.ActivityFeedItemDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@Transactional(readOnly = true)
public class FeedService {
    private final UserRepository userRepository;
    private final PromptRepository promptRepository;
    private final PromptMapper promptMapper;
    public FeedService(UserRepository userRepository, PromptRepository promptRepository, PromptMapper promptMapper) {
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.promptMapper = promptMapper;
    }
    public Page<ActivityFeedItemDto> getFeedForUser(UserDetails principal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); 
        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
        if (currentUser.getFollowing() == null || currentUser.getFollowing().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<UUID> followedUserIds = currentUser.getFollowing().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        Page<Prompt> promptsFromFollowing = promptRepository.findByAuthor_IdInOrderByCreatedAtDesc(followedUserIds, pageable);
        return promptsFromFollowing.map(prompt -> new ActivityFeedItemDto(
                "NEW_PROMPT_FROM_FOLLOWING",
                prompt.getCreatedAt(),
                promptMapper.toDto(prompt, currentUser)
        ));
    }
}