package com.sportsms.team;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamDto.TeamResponse> list() {
        return teamService.findAll().stream()
                .map(team -> new TeamDto.TeamResponse(
                        team.getId(),
                        team.getName(),
                        team.getClub() != null ? team.getClub().getId() : null,
                        team.getClub() != null ? team.getClub().getName() : null,
                        team.getCoachName(),
                        team.getHomeGround(),
                        team.getLogoUrl(),
                        team.getCreatedAt()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public TeamDto.TeamResponse create(@Valid @RequestBody TeamDto.TeamRequest request) {
        Team team = teamService.create(request);
        return new TeamDto.TeamResponse(team.getId(), team.getName(),
                team.getClub() != null ? team.getClub().getId() : null,
                team.getClub() != null ? team.getClub().getName() : null,
                team.getCoachName(), team.getHomeGround(), team.getLogoUrl(), team.getCreatedAt());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public TeamDto.TeamResponse update(@PathVariable("id") UUID id, @Valid @RequestBody TeamDto.TeamRequest request) {
        Team team = teamService.update(id, request);
        return new TeamDto.TeamResponse(team.getId(), team.getName(),
                team.getClub() != null ? team.getClub().getId() : null,
                team.getClub() != null ? team.getClub().getName() : null,
                team.getCoachName(), team.getHomeGround(), team.getLogoUrl(), team.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") UUID id) {
        teamService.delete(id);
    }
}
