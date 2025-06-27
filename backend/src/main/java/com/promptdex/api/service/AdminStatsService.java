package com.promptdex.api.service;

import com.promptdex.api.dto.StatsDto;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminStatsService {
    private final UserRepository userRepository;
    private final PromptRepository promptRepository;
    private final ReviewRepository reviewRepository;

    public AdminStatsService(
            UserRepository userRepository,
            PromptRepository promptRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.promptRepository = promptRepository;
        this.reviewRepository = reviewRepository;
    }

    public StatsDto getGlobalStats() {
        long totalUsers = userRepository.count();
        long totalPrompts = promptRepository.count();
        long totalReviews = reviewRepository.count();
        return new StatsDto(totalUsers, totalPrompts, totalReviews);
    }
}