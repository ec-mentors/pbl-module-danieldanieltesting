package com.promptdex.api.repository;

import com.promptdex.api.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Finds a set of tags by their names, performing a case-insensitive search.
     *
     * @param names A set of tag names to search for.
     * @return A Set of matching Tag entities.
     */
    Set<Tag> findByNameInIgnoreCase(Set<String> names);
}