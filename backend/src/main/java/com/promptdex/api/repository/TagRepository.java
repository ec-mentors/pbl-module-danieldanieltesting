package com.promptdex.api.repository;
import com.promptdex.api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import java.util.UUID;
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Set<Tag> findByNameInIgnoreCase(Set<String> names);
}