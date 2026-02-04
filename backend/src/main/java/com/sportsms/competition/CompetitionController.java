package com.sportsms.competition;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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
@RequestMapping("/api/competitions")
public class CompetitionController {
    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/seasons")
    public List<CompetitionDto.SeasonResponse> listSeasons() {
        return competitionService.listSeasons().stream()
                .map(season -> new CompetitionDto.SeasonResponse(season.getId(), season.getName(), season.getStartDate(), season.getEndDate()))
                .toList();
    }

    @PostMapping("/seasons")
    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionDto.SeasonResponse createSeason(@Valid @RequestBody CompetitionDto.SeasonRequest request) {
        Season season = competitionService.createSeason(request);
        return new CompetitionDto.SeasonResponse(season.getId(), season.getName(), season.getStartDate(), season.getEndDate());
    }

    @PutMapping("/seasons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionDto.SeasonResponse updateSeason(@PathVariable("id") UUID id,
                                                      @Valid @RequestBody CompetitionDto.SeasonRequest request) {
        Season season = competitionService.updateSeason(id, request);
        return new CompetitionDto.SeasonResponse(season.getId(), season.getName(), season.getStartDate(), season.getEndDate());
    }

    @DeleteMapping("/seasons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSeason(@PathVariable("id") UUID id) {
        competitionService.deleteSeason(id);
    }

    @GetMapping
    public List<CompetitionDto.CompetitionResponse> listCompetitions() {
        return competitionService.listCompetitions().stream()
                .map(competition -> new CompetitionDto.CompetitionResponse(
                        competition.getId(),
                        competition.getName(),
                        competition.getType(),
                        competition.getTeams().stream().map(team -> team.getId()).collect(java.util.stream.Collectors.toSet())))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionDto.CompetitionResponse createCompetition(@Valid @RequestBody CompetitionDto.CompetitionRequest request) {
        Competition competition = competitionService.createCompetition(request);
        return new CompetitionDto.CompetitionResponse(competition.getId(), competition.getName(), competition.getType(),
                competition.getTeams().stream().map(team -> team.getId()).collect(java.util.stream.Collectors.toSet()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompetitionDto.CompetitionResponse updateCompetition(@PathVariable("id") UUID id,
                                                                @Valid @RequestBody CompetitionDto.CompetitionRequest request) {
        Competition competition = competitionService.updateCompetition(id, request);
        Set<UUID> teamIds = competition.getTeams().stream().map(team -> team.getId()).collect(java.util.stream.Collectors.toSet());
        return new CompetitionDto.CompetitionResponse(competition.getId(), competition.getName(), competition.getType(), teamIds);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCompetition(@PathVariable("id") UUID id) {
        competitionService.deleteCompetition(id);
    }
}
