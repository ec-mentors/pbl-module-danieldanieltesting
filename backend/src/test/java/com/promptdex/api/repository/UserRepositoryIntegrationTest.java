package com.promptdex.api.repository;

import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Prompt prompt1;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = new User();
        user1.setUsername("userOne");
        user1.setEmail("userone@test.com");
        user1.setProvider(AuthProvider.LOCAL);

        user2 = new User();
        user2.setUsername("userTwo");
        user2.setEmail("usertwo@test.com");
        user2.setProvider(AuthProvider.LOCAL);

        entityManager.persist(user1);
        entityManager.persist(user2);

        // Create a prompt authored by user2
        prompt1 = new Prompt();
        prompt1.setTitle("Test Prompt");
        prompt1.setPromptText("Test prompt text.");
        prompt1.setCategory("Testing");
        prompt1.setTargetAiModel("GPT-4");
        prompt1.setAuthor(user2);
        entityManager.persist(prompt1);

        // Establish relationships
        user1.follow(user2); // Use the entity helper method to set both sides of the relationship
        user1.getBookmarkedPrompts().add(prompt1);

        // Persist the changes to the relationships
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure fresh data is loaded by queries
    }

    @Test
    void whenFindByUsernameWithFollowing_thenFollowingCollectionShouldBeFetched() {
        // WHEN fetching userOne with their 'following' list
        Optional<User> foundUserOpt = userRepository.findByUsernameWithFollowing("userOne");

        // THEN the user should be found
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();

        // AND their 'following' collection should be initialized and contain userTwo
        assertThat(foundUser.getFollowing()).hasSize(1);
        assertThat(foundUser.getFollowing().iterator().next().getUsername()).isEqualTo("userTwo");
    }

    @Test
    void whenFindByIdWithFollowers_thenFollowersCollectionShouldBeFetched() {
        // WHEN fetching userTwo with their 'followers' list
        Optional<User> foundUserOpt = userRepository.findByIdWithFollowers(user2.getId());

        // THEN the user should be found
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();

        // AND their 'followers' collection should be initialized and contain userOne
        assertThat(foundUser.getFollowers()).hasSize(1);
        assertThat(foundUser.getFollowers().iterator().next().getUsername()).isEqualTo("userOne");
    }

    @Test
    void whenFindByUsernameWithBookmarks_thenBookmarksCollectionShouldBeFetched() {
        // WHEN fetching userOne with their bookmarks
        Optional<User> foundUserOpt = userRepository.findByUsernameWithBookmarks("userOne");

        // THEN the user should be found
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();

        // AND their bookmarks collection should be initialized and contain the correct prompt
        assertThat(foundUser.getBookmarkedPrompts()).hasSize(1);
        assertThat(foundUser.getBookmarkedPrompts().iterator().next().getTitle()).isEqualTo("Test Prompt");
    }
}