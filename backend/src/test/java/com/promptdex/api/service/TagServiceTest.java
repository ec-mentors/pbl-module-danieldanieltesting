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
        Set<String> inputTagNames = Set.of("SciFi", "horror", "NEW-TAG");
        Tag existingTag = new Tag();
        existingTag.setName("scifi");
        Set<Tag> existingTagsFromRepo = new HashSet<>();
        existingTagsFromRepo.add(existingTag);
        when(tagRepository.findByNameInIgnoreCase(Set.of("scifi", "horror", "new-tag")))
                .thenReturn(existingTagsFromRepo);
        Set<Tag> resultTags = tagService.findOrCreateTags(inputTagNames);
        assertEquals(3, resultTags.size());
        Set<String> resultTagNames = resultTags.stream().map(Tag::getName).collect(Collectors.toSet());
        assertTrue(resultTagNames.contains("scifi"));
        assertTrue(resultTagNames.contains("horror"));
        assertTrue(resultTagNames.contains("new-tag"));
        verify(tagRepository, times(1)).saveAll(any());
    }

    @Test
    void findOrCreateTags_withOnlyExistingTags_shouldNotCallSaveAll() {
        Set<String> inputTagNames = Set.of("Existing1", "existing2");
        Tag tag1 = new Tag();
        tag1.setName("existing1");
        Tag tag2 = new Tag();
        tag2.setName("existing2");
        Set<Tag> existingTagsFromRepo = Set.of(tag1, tag2);
        when(tagRepository.findByNameInIgnoreCase(Set.of("existing1", "existing2")))
                .thenReturn(existingTagsFromRepo);
        Set<Tag> resultTags = tagService.findOrCreateTags(inputTagNames);
        assertEquals(2, resultTags.size());
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    void findOrCreateTags_withEmptySet_shouldReturnEmptySet() {
        Set<Tag> resultTags = tagService.findOrCreateTags(Collections.emptySet());
        assertTrue(resultTags.isEmpty());
        verify(tagRepository, never()).findByNameInIgnoreCase(any());
        verify(tagRepository, never()).saveAll(any());
    }
}