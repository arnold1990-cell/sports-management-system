package com.sportsms.standings;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/standings")
public class StandingsController {
    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping("/{competitionId}/{seasonId}")
    public StandingsDto.StandingsResponse getStandings(@PathVariable UUID competitionId, @PathVariable UUID seasonId) {
        return standingsService.getStandings(competitionId, seasonId);
    }
}
