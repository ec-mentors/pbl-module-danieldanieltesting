package com.promptdex.api.repository;

import com.promptdex.api.model.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, UUID> {
    @Query(value = "SELECT DISTINCT p FROM Prompt p " +
            "LEFT JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.tags t WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.promptText) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "(:tags IS NULL OR t.name IN :tags)",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Prompt p LEFT JOIN p.tags t WHERE " +
                    "(:searchTerm IS NULL OR :searchTerm = '' OR " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.promptText) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
                    "(:tags IS NULL OR t.name IN :tags)")
    Page<Prompt> searchAndPagePrompts(
            @Param("searchTerm") String searchTerm,
            @Param("tags") List<String> tags,
            Pageable pageable
    );

    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.author WHERE p.author.username = :username")
    Page<Prompt> findByAuthor_Username(@Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.author JOIN p.bookmarkedByUsers u WHERE u.username = :username")
    Page<Prompt> findByBookmarkedByUsers_Username(@Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.author WHERE p.author.id IN :authorIds ORDER BY p.createdAt DESC")
    Page<Prompt> findByAuthor_IdInOrderByCreatedAtDesc(@Param("authorIds") List<UUID> authorIds, Pageable pageable);

    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.tags WHERE p.id = :promptId")
    Optional<Prompt> findByIdWithAuthorAndTags(@Param("promptId") UUID promptId);

    @Query(value = "SELECT DISTINCT p FROM Prompt p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.tags",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Prompt p")
    Page<Prompt> findAllWithAuthorAndTags(Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.prompt.id = :promptId")
    Optional<Double> findAverageRatingByPromptId(@Param("promptId") UUID promptId);

    @Query(value = "SELECT DISTINCT p FROM Prompt p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH p.tags t " +
            "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.promptText) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Prompt p " +
                    "LEFT JOIN p.author a " +
                    "LEFT JOIN p.tags t " +
                    "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.promptText) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Prompt> findAllAdminSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
}