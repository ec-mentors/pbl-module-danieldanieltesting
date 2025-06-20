package com.promptdex.api.repository;

import com.promptdex.api.model.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PromptRepository extends JpaRepository<Prompt, UUID> {

    @Query("SELECT p FROM Prompt p WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.promptText) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Prompt> searchAndPagePrompts(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Prompt> findByAuthor_Username(String username, Pageable pageable);

    /**
     * Finds a paginated list of prompts that have been bookmarked by a specific user.
     * This query traverses the many-to-many relationship.
     *
     * @param username The username of the user whose bookmarks are being fetched.
     * @param pageable The pagination information.
     * @return A Page of bookmarked Prompts.
     */
    Page<Prompt> findByBookmarkedByUsers_Username(String username, Pageable pageable);
}