package com.promptdex.api.service;

import com.promptdex.api.dto.CreatePromptRequest;
import com.promptdex.api.dto.PromptDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.PromptMapper;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Tag;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final PromptMapper promptMapper;

    public PromptService(PromptRepository promptRepository, UserRepository userRepository, TagService tagService, PromptMapper promptMapper) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
        this.tagService = tagService;
        this.promptMapper = promptMapper;
    }

    private User getOptionalUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Authentication is required to perform this action.");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User " + userDetails.getUsername() + " not found in database."));
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> searchAndPagePrompts(String searchTerm, List<String> tags, int page, int size, UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<String> lowerCaseTags = (tags != null && !tags.isEmpty()) ? tags.stream().map(String::toLowerCase).collect(Collectors.toList()) : null;
        Page<Prompt> promptPage = promptRepository.searchAndPagePrompts(searchTerm, lowerCaseTags, pageable);

        User currentUser = getOptionalUser(userDetails);
        return promptPage.map(prompt -> promptMapper.toDto(prompt, currentUser));
    }

    @Transactional(readOnly = true)
    public PromptDto getPromptById(UUID promptId, UserDetails userDetails) {
        Prompt prompt = promptRepository.findByIdWithAuthorAndTags(promptId) // Ensure tags and author are fetched
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        User currentUser = getOptionalUser(userDetails);
        return promptMapper.toDto(prompt, currentUser);
    }

    @Transactional
    public PromptDto createPrompt(CreatePromptRequest request, UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        Prompt prompt = new Prompt();
        prompt.setTitle(request.title());
        prompt.setPromptText(request.text());
        prompt.setDescription(request.description());
        prompt.setTargetAiModel(request.model());
        prompt.setCategory(request.category());
        prompt.setAuthor(user);
        Prompt savedPrompt = promptRepository.saveAndFlush(prompt);
        return promptMapper.toDto(savedPrompt, user);
    }

    @Transactional
    public PromptDto updatePrompt(UUID promptId, CreatePromptRequest request, UserDetails userDetails) throws AccessDeniedException {
        User user = getUserFromDetails(userDetails);
        Prompt prompt = promptRepository.findByIdWithAuthorAndTags(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        if (!prompt.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this prompt.");
        }
        prompt.setTitle(request.title());
        prompt.setPromptText(request.text());
        prompt.setDescription(request.description());
        prompt.setTargetAiModel(request.model());
        prompt.setCategory(request.category());
        Prompt updatedPrompt = promptRepository.save(prompt);
        return promptMapper.toDto(updatedPrompt, user);
    }

    @Transactional
    public PromptDto updatePromptTags(UUID promptId, Set<String> tagNames, UserDetails userDetails) throws AccessDeniedException {
        User user = getUserFromDetails(userDetails);
        Prompt prompt = promptRepository.findByIdWithAuthorAndTags(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        if (!prompt.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to edit tags for this prompt.");
        }
        Set<Tag> managedTags = tagService.findOrCreateTags(tagNames);
        prompt.setTags(managedTags);
        Prompt savedPrompt = promptRepository.save(prompt);
        return promptMapper.toDto(savedPrompt, user);
    }

    @Transactional
    public void deletePrompt(UUID promptId, UserDetails userDetails) throws AccessDeniedException {
        User user = getUserFromDetails(userDetails);
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        if (!prompt.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this prompt.");
        }
        promptRepository.delete(prompt);
    }

    // Bookmark methods... (remain unchanged)
    @Transactional
    public void addBookmark(UUID promptId, String username) {
        User user = userRepository.findByUsernameWithBookmarks(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        user.getBookmarkedPrompts().add(prompt);
    }

    @Transactional
    public void removeBookmark(UUID promptId, String username) {
        User user = userRepository.findByUsernameWithBookmarks(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        user.getBookmarkedPrompts().remove(prompt);
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> getBookmarkedPrompts(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Prompt> promptPage = promptRepository.findByBookmarkedByUsers_Username(username, pageable);
        return promptPage.map(prompt -> promptMapper.toDto(prompt, user));
    }

    @Transactional(readOnly = true)
    public Page<PromptDto> getPromptsByAuthorUsername(String username, int page, int size, UserDetails userDetails) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with username: " + username));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Prompt> promptPage = promptRepository.findByAuthor_Username(username, pageable);
        User currentUser = getOptionalUser(userDetails);
        return promptPage.map(prompt -> promptMapper.toDto(prompt, currentUser));
    }

    // --- UPDATED ADMIN METHOD ---
    @Transactional(readOnly = true)
    public Page<PromptDto> getAllPromptsAsAdmin(String searchTerm, Pageable pageable, UserDetails principal) {
        Page<Prompt> promptsPage;
        if (StringUtils.hasText(searchTerm)) {
            promptsPage = promptRepository.findAllAdminSearch(searchTerm.trim(), pageable);
        } else {
            promptsPage = promptRepository.findAllWithAuthorAndTags(pageable);
        }
        User currentAdminUser = getOptionalUser(principal);
        // The promptMapper.toDto should correctly handle the Prompt entity (with fetched author/tags)
        return promptsPage.map(prompt -> promptMapper.toDto(prompt, currentAdminUser));
    }

    @Transactional
    public void deletePromptAsAdmin(UUID promptId) {
        Prompt promptToDelete = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt not found with id: " + promptId));
        promptRepository.delete(promptToDelete);
    }
    // --- END ADMIN METHODS ---
}