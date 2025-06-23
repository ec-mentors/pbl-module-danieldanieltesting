package com.promptdex.api.repository;

import com.promptdex.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // Added @Repository for consistency
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.bookmarkedPrompts WHERE u.username = :username")
    Optional<User> findByUsernameWithBookmarks(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.following WHERE u.username = :username")
    Optional<User> findByUsernameWithFollowing(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followers WHERE u.id = :id")
    Optional<User> findByIdWithFollowers(@Param("id") UUID id);

    // --- NEW METHOD FOR ADMIN SEARCH ---
    @Query(value = "SELECT u FROM User u WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(u) FROM User u WHERE " +
                    "(:searchTerm IS NULL OR :searchTerm = '' OR " +
                    "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findAllAdminSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
}