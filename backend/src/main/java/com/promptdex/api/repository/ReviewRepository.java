package com.promptdex.api.repository;

import com.promptdex.api.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByPrompt_IdAndUser_Username(UUID promptId, String username);

    boolean existsByPrompt_IdAndUser_Id(UUID promptId, UUID userId);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.prompt",
            countQuery = "SELECT count(r) FROM Review r")
    Page<Review> findAllWithUserAndPrompt(Pageable pageable);

    // --- NEW METHOD FOR ADMIN REVIEW SEARCH ---
    @Query(value = "SELECT r FROM Review r " +
            "JOIN FETCH r.user u " + // Join with User for username search
            "JOIN FETCH r.prompt p " + // Join with Prompt for title search
            "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "JOIN r.user u " +
                    "JOIN r.prompt p " +
                    "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
                    "LOWER(r.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Review> findAllAdminSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
}