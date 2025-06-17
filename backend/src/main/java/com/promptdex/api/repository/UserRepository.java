package com.promptdex.api.repository;

import com.promptdex.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// JpaRepository<EntityType, PrimaryKeyType>
public interface UserRepository extends JpaRepository<User, UUID> {

    // Spring Data JPA will automatically implement these methods based on their names.
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}