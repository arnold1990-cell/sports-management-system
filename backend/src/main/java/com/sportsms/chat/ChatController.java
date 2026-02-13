package com.sportsms.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @GetMapping("/rooms") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public List<Map<String, String>> rooms() { return List.of(Map.of("id", UUID.randomUUID().toString(), "name", "League-wide room")); }

    @GetMapping("/rooms/{roomId}/messages") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public List<Map<String, String>> messages(@PathVariable UUID roomId) { return List.of(Map.of("roomId", roomId.toString(), "content", "Chat persistence scaffolded")); }

    @PostMapping("/rooms/{roomId}/messages") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public Map<String, String> send(@PathVariable UUID roomId, @RequestBody Map<String, String> payload) { return Map.of("status", "saved", "roomId", roomId.toString(), "content", payload.getOrDefault("content", "")); }

    @GetMapping("/ai/summarize") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','COACH')")
    public Map<String, String> summarize() { return Map.of("summary", "AI not configured"); }
}
