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
    Optional<Collection> findByIdAndOwner_Username(UUID id, String username);
    List<Collection> findByOwner_UsernameOrderByNameAsc(String username);
    boolean existsByNameAndOwner_Id(String name, UUID ownerId);
    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.prompts WHERE c.id = :id")
    Optional<Collection> findByIdWithPrompts(@Param("id") UUID id);
}