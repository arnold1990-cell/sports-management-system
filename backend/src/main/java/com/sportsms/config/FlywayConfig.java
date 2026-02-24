package com.sportsms.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(
            @Value("${app.flyway.repair-on-startup:true}") boolean repairOnStartup
    ) {
        return flyway -> migrateWithOptionalRepair(flyway, repairOnStartup);
    }

    private static void migrateWithOptionalRepair(Flyway flyway, boolean repairOnStartup) {
        if (repairOnStartup) {
            flyway.repair();
        }
        flyway.migrate();
    }
}
