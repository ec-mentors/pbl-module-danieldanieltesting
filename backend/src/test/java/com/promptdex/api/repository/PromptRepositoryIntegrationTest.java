package com.promptdex.api.repository;

import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Tag;
import com.promptdex.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PromptRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PromptRepository promptRepository;
    private Prompt prompt1, prompt2, prompt3, prompt4;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setProvider(AuthProvider.LOCAL);
        entityManager.persist(user);
        Tag tagJava = new Tag(null, "java", null);
        Tag tagPython = new Tag(null, "python", null);
        Tag tagTesting = new Tag(null, "testing", null);
        entityManager.persist(tagJava);
        entityManager.persist(tagPython);
        entityManager.persist(tagTesting);
        prompt1 = new Prompt();
        prompt1.setTitle("Java Basics");
        prompt1.setPromptText("Explain OOP in Java.");
        prompt1.setCategory("Programming");
        prompt1.setTargetAiModel("GPT-4");
        prompt1.setAuthor(user);
        prompt1.setTags(Set.of(tagJava));
        prompt2 = new Prompt();
        prompt2.setTitle("Python Scripting");
        prompt2.setPromptText("Write a Python script.");
        prompt2.setCategory("Programming");
        prompt2.setTargetAiModel("Claude 3");
        prompt2.setAuthor(user);
        prompt2.setTags(Set.of(tagPython));
        prompt3 = new Prompt();
        prompt3.setTitle("Advanced Testing");
        prompt3.setPromptText("A prompt about advanced java testing techniques.");
        prompt3.setDescription("A prompt about testing with JUnit and Mockito.");
        prompt3.setCategory("Testing");
        prompt3.setTargetAiModel("GPT-4");
        prompt3.setAuthor(user);
        prompt3.setTags(Set.of(tagJava, tagTesting));
        prompt4 = new Prompt();
        prompt4.setTitle("SQL Queries");
        prompt4.setPromptText("A prompt about SQL.");
        prompt4.setDescription("A prompt about databases.");
        prompt4.setCategory("Database");
        prompt4.setTargetAiModel("Any");
        prompt4.setAuthor(user);
        entityManager.persist(prompt1);
        entityManager.persist(prompt2);
        entityManager.persist(prompt3);
        entityManager.persist(prompt4);
        entityManager.flush();
    }

    @Test
    void whenSearchTermProvided_thenReturnMatchingPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> results = promptRepository.searchAndPagePrompts("java", null, pageable);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent()).extracting(Prompt::getTitle).containsExactlyInAnyOrder("Java Basics", "Advanced Testing");
    }

    @Test
    void whenSearchTermIsCaseInsensitive_thenReturnMatchingPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> results = promptRepository.searchAndPagePrompts("bAsIcS", null, pageable);
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Java Basics");
    }

    @Test
    void whenTagsProvided_thenReturnMatchingPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> results = promptRepository.searchAndPagePrompts(null, List.of("java"), pageable);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent()).extracting(Prompt::getTitle).containsExactlyInAnyOrder("Java Basics", "Advanced Testing");
    }

    @Test
    void whenSearchTermAndTagsProvided_thenReturnMatchingPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> results = promptRepository.searchAndPagePrompts("testing", List.of("java"), pageable);
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Advanced Testing");
    }

    @Test
    void whenNoFiltersProvided_thenReturnAllPrompts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> results = promptRepository.searchAndPagePrompts(null, null, pageable);
        assertThat(results.getTotalElements()).isEqualTo(4);
    }

    @Test
    void whenPaginationIsApplied_thenReturnCorrectSlice() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Prompt> results = promptRepository.searchAndPagePrompts(null, null, pageable);
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(4);
        assertThat(results.getTotalPages()).isEqualTo(2);
    }
}