package com.promptdex.api.service;

import com.promptdex.api.model.Tag;
import com.promptdex.api.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void findOrCreateTags_withMixedCaseAndNewTags_shouldReturnCorrectSet() {
        // Arrange
        // Input has mixed case and a mix of existing and new tags
        Set<String> inputTagNames = Set.of("SciFi", "horror", "NEW-TAG");

        // Mock the existing tag "scifi" (note the lowercase)
        Tag existingTag = new Tag();
        existingTag.setName("scifi");
        Set<Tag> existingTagsFromRepo = new HashSet<>();
        existingTagsFromRepo.add(existingTag);

        // When the repo is searched with lowercase names, it finds "scifi"
        when(tagRepository.findByNameInIgnoreCase(Set.of("scifi", "horror", "new-tag")))
                .thenReturn(existingTagsFromRepo);

        // Act
        Set<Tag> resultTags = tagService.findOrCreateTags(inputTagNames);

        // Assert
        // We expect 3 tags in total: scifi, horror, new-tag
        assertEquals(3, resultTags.size());

        Set<String> resultTagNames = resultTags.stream().map(Tag::getName).collect(Collectors.toSet());
        assertTrue(resultTagNames.contains("scifi"));
        assertTrue(resultTagNames.contains("horror"));
        assertTrue(resultTagNames.contains("new-tag"));

        // Verify that saveAll was called for the new tags ("horror" and "new-tag")
        verify(tagRepository, times(1)).saveAll(any());
    }

    @Test
    void findOrCreateTags_withOnlyExistingTags_shouldNotCallSaveAll() {
        // Arrange
        Set<String> inputTagNames = Set.of("Existing1", "existing2");

        Tag tag1 = new Tag();
        tag1.setName("existing1");
        Tag tag2 = new Tag();
        tag2.setName("existing2");
        Set<Tag> existingTagsFromRepo = Set.of(tag1, tag2);

        when(tagRepository.findByNameInIgnoreCase(Set.of("existing1", "existing2")))
                .thenReturn(existingTagsFromRepo);

        // Act
        Set<Tag> resultTags = tagService.findOrCreateTags(inputTagNames);

        // Assert
        assertEquals(2, resultTags.size());
        // Verify that saveAll was NEVER called, since no new tags were created
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    void findOrCreateTags_withEmptySet_shouldReturnEmptySet() {
        // Act
        Set<Tag> resultTags = tagService.findOrCreateTags(Collections.emptySet());

        // Assert
        assertTrue(resultTags.isEmpty());
        verify(tagRepository, never()).findByNameInIgnoreCase(any());
        verify(tagRepository, never()).saveAll(any());
    }
}