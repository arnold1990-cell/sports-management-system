package com.sportsms.fixture;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fixtures")
public class FixtureController {
    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    @GetMapping("/public")
    public List<FixtureDto.FixtureResponse> listPublic(
            @RequestParam(name = "competitionId", required = false) UUID competitionId,
            @RequestParam(name = "seasonId", required = false) UUID seasonId,
            @RequestParam(name = "teamId", required = false) UUID teamId,
            @RequestParam(name = "from", required = false) OffsetDateTime from,
            @RequestParam(name = "to", required = false) OffsetDateTime to) {
        return fixtureService.list(competitionId, seasonId, teamId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH','REFEREE')")
    public List<FixtureDto.FixtureResponse> list(
            @RequestParam(name = "competitionId", required = false) UUID competitionId,
            @RequestParam(name = "seasonId", required = false) UUID seasonId,
            @RequestParam(name = "teamId", required = false) UUID teamId,
            @RequestParam(name = "from", required = false) OffsetDateTime from,
            @RequestParam(name = "to", required = false) OffsetDateTime to) {
        return fixtureService.list(competitionId, seasonId, teamId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public FixtureDto.FixtureResponse create(@Valid @RequestBody FixtureDto.FixtureCreateRequest request) {
        Fixture fixture = fixtureService.create(request);
        return toResponse(fixture);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH','REFEREE')")
    public FixtureDto.FixtureResponse update(@PathVariable("id") UUID id,
                                             @Valid @RequestBody FixtureDto.FixtureRequest request) {
        Fixture fixture = fixtureService.update(id, request);
        return toResponse(fixture);
    }

    private FixtureDto.FixtureResponse toResponse(Fixture fixture) {
        return new FixtureDto.FixtureResponse(
                fixture.getId(),
                fixture.getHomeTeam().getId(),
                fixture.getHomeTeam().getName(),
                fixture.getAwayTeam().getId(),
                fixture.getAwayTeam().getName(),
                fixture.getCompetition().getId(),
                fixture.getCompetition().getName(),
                fixture.getSeason().getId(),
                fixture.getSeason().getName(),
                fixture.getReferee() != null ? fixture.getReferee().getId() : null,
                fixture.getReferee() != null ? fixture.getReferee().getFullName() : null,
                fixture.getVenue(),
                fixture.getMatchDate(),
                fixture.getStatus(),
                fixture.getHomeScore(),
                fixture.getAwayScore(),
                fixture.getGoals().stream()
                        .map(goal -> new FixtureDto.GoalEventResponse(
                                goal.getId(),
                                goal.getTeam().getId(),
                                goal.getTeam().getName(),
                                goal.getScorerName(),
                                goal.getMinute()))
                        .toList());
    }
}
