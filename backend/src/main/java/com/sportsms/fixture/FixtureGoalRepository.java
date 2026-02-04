package com.sportsms.fixture;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureGoalRepository extends JpaRepository<FixtureGoal, UUID> {
}
