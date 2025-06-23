package com.promptdex.api.service;

import com.promptdex.api.dto.ActivityFeedItemDto;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<ActivityFeedItemDto> getFeedForUser(UserPrincipal principal, int page, int size) {
        // Step 1: Get the current user and the list of users they follow.
        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));

        // If the user isn't following anyone, return an empty page.
        if (currentUser.getFollowing() == null || currentUser.getFollowing().isEmpty()) {
            return Page.empty();
        }

        // Step 2: Extract the IDs of the users being followed.
        List<UUID> followedUserIds = currentUser.getFollowing().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // Step 3: Use the repository to find all prompts from those users, paginated.
        Pageable pageable = PageRequest.of(page, size);
        Page<Prompt> promptsFromFollowing = promptRepository.findByAuthor_IdInOrderByCreatedAtDesc(followedUserIds, pageable);

        // Step 4: Map the Prompt entities to ActivityFeedItemDto objects.
        // We pass `currentUser` to the mapper so it can correctly determine the `isBookmarked` status for each prompt.
        return promptsFromFollowing.map(prompt -> new ActivityFeedItemDto(
                "NEW_PROMPT_FROM_FOLLOWING",
                prompt.getCreatedAt(),
                promptMapper.toDto(prompt, currentUser)
        ));
    }
}