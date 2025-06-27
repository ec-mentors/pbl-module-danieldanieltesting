package com.promptdex.api.service;
import com.promptdex.api.model.Tag;
import com.promptdex.api.repository.TagRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class TagService {
    private final TagRepository tagRepository;
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    @Transactional(readOnly = true)
    public List<String> getAllTagNames() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
    @Transactional
    public Set<Tag> findOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> normalizedTagNames = tagNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<Tag> existingTags = tagRepository.findByNameInIgnoreCase(normalizedTagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        Set<Tag> newTagsToCreate = normalizedTagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder().name(name).build())
                .collect(Collectors.toSet());
        if (!newTagsToCreate.isEmpty()) {
            tagRepository.saveAll(newTagsToCreate);
            existingTags.addAll(newTagsToCreate);
        }
        return existingTags;
    }
}