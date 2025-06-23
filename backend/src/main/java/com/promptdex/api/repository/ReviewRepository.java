package com.promptdex.api.repository;

import com.promptdex.api.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByPrompt_IdAndUser_Username(UUID promptId, String username);

    boolean existsByPrompt_IdAndUser_Id(UUID promptId, UUID userId);
}