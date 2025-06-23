package com.promptdex.api.controller.admin;

import com.promptdex.api.dto.StatsDto;
import com.promptdex.api.service.AdminStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    public AdminStatsController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    /**
     * GET /api/admin/stats : Get global application statistics.
     *
     * @return ResponseEntity with StatsDto.
     */
    @GetMapping
    public ResponseEntity<StatsDto> getGlobalStats() {
        StatsDto stats = adminStatsService.getGlobalStats();
        return ResponseEntity.ok(stats);
    }
}