package com.sportsms.competition;

import com.sportsms.common.NotFoundException;
import com.sportsms.team.Team;
import com.sportsms.team.TeamRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;

    public CompetitionService(CompetitionRepository competitionRepository, SeasonRepository seasonRepository,
                              TeamRepository teamRepository) {
        this.competitionRepository = competitionRepository;
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
    }

    public List<Season> listSeasons() {
        return seasonRepository.findAll();
    }

    public Season createSeason(CompetitionDto.SeasonRequest request) {
        Season season = new Season();
        season.setName(request.name());
        season.setStartDate(request.startDate());
        season.setEndDate(request.endDate());
        return seasonRepository.save(season);
    }

    public Season updateSeason(UUID id, CompetitionDto.SeasonRequest request) {
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        season.setName(request.name());
        season.setStartDate(request.startDate());
        season.setEndDate(request.endDate());
        return seasonRepository.save(season);
    }

    public void deleteSeason(UUID id) {
        if (!seasonRepository.existsById(id)) {
            throw new NotFoundException("Season not found");
        }
        seasonRepository.deleteById(id);
    }

    public List<Competition> listCompetitions() {
        return competitionRepository.findAll();
    }

    public Competition createCompetition(CompetitionDto.CompetitionRequest request) {
        Competition competition = new Competition();
        applyCompetitionRequest(competition, request);
        return competitionRepository.save(competition);
    }

    public Competition updateCompetition(UUID id, CompetitionDto.CompetitionRequest request) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Competition not found"));
        applyCompetitionRequest(competition, request);
        return competitionRepository.save(competition);
    }

    public void deleteCompetition(UUID id) {
        if (!competitionRepository.existsById(id)) {
            throw new NotFoundException("Competition not found");
        }
        competitionRepository.deleteById(id);
    }

    private void applyCompetitionRequest(Competition competition, CompetitionDto.CompetitionRequest request) {
        competition.setName(request.name());
        competition.setType(request.type());
        if (request.teamIds() != null) {
            Set<Team> teams = request.teamIds().stream()
                    .map(teamId -> teamRepository.findById(teamId)
                            .orElseThrow(() -> new NotFoundException("Team not found")))
                    .collect(Collectors.toSet());
            competition.setTeams(teams);
        }
    }
}
