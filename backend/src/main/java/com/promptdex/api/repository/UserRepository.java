package com.promptdex.api.repository;

import com.promptdex.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    /**
     * Finds a user by their username and eagerly fetches the associated 'bookmarkedPrompts'
     * collection in a single query to prevent N+1 performance issues.
     *
     * @param username The username to search for.
     * @return An Optional containing the User with their bookmarks pre-loaded.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.bookmarkedPrompts WHERE u.username = :username")
    Optional<User> findByUsernameWithBookmarks(@Param("username") String username);
}