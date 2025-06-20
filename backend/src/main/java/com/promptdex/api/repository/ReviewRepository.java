// src/main/java/com/promptdex/api/repository/ReviewRepository.java
package com.promptdex.api.repository;

import com.promptdex.api.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Checks if a review exists for a given prompt ID and a specific user's username.
     * Spring Data JPA automatically creates the query based on the method name.
     * "Prompt_Id" traverses the 'prompt' entity to its 'id' field.
     * "User_Username" traverses the 'user' entity to its 'username' field.
     *
     * @param promptId The ID of the prompt.
     * @param username The username of the user.
     * @return true if a review exists, false otherwise.
     */
    boolean existsByPrompt_IdAndUser_Username(UUID promptId, String username);

}