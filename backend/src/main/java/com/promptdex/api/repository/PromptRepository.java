package com.promptdex.api.repository;

import com.promptdex.api.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PromptRepository extends JpaRepository<Prompt, UUID> {
}