package com.sportsms.analytics;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @GetMapping("/subscriptions") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<Map<String, Object>> subscriptions() { return List.of(Map.of("month", "2026-01", "revenue", 0)); }
}
