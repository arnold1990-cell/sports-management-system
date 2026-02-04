package com.sportsms.fixture;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FixtureRepository extends JpaRepository<Fixture, UUID> {
    @Query("select f from Fixture f where (:competitionId is null or f.competition.id = :competitionId) " +
            "and (:seasonId is null or f.season.id = :seasonId) " +
            "and (:teamId is null or f.homeTeam.id = :teamId or f.awayTeam.id = :teamId) " +
            "and (:fromDate is null or f.matchDate >= :fromDate) " +
            "and (:toDate is null or f.matchDate <= :toDate)")
    List<Fixture> filterFixtures(@Param("competitionId") UUID competitionId,
                                 @Param("seasonId") UUID seasonId,
                                 @Param("teamId") UUID teamId,
                                 @Param("fromDate") OffsetDateTime fromDate,
                                 @Param("toDate") OffsetDateTime toDate);
}
