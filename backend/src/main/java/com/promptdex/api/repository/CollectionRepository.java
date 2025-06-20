// src/main/java/com/promptdex/api/repository/CollectionRepository.java
package com.promptdex.api.repository;

import com.promptdex.api.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    // Find a specific collection by its ID and the owner's username for permission checks.
    Optional<Collection> findByIdAndOwner_Username(UUID id, String username);

    // Find all collections for a given user, ordered by name.
    List<Collection> findByOwner_UsernameOrderByNameAsc(String username);

    // Check if a collection with the same name already exists for a specific user.
    boolean existsByNameAndOwner_Id(String name, UUID ownerId);

    // Optimized query to fetch a collection and its associated prompts in a single database trip.
    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.prompts WHERE c.id = :id")
    Optional<Collection> findByIdWithPrompts(@Param("id") UUID id);
}