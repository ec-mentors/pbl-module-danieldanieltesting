package com.promptdex.api.repository;

import com.promptdex.api.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}