package com.sportsms.competition;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
}
