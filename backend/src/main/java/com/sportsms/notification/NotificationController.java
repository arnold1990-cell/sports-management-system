package com.sportsms.notification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public List<Map<String, Object>> list() {
        return List.of(Map.of("id", UUID.randomUUID(), "title", "Reminder", "message", "Renewal due", "createdAt", OffsetDateTime.now(), "readAt", null));
    }
}
