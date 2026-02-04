package com.sportsms.auth;

import java.util.Set;
import java.util.UUID;

public record AuthProfileResponse(UUID id, String email, String fullName, Set<String> roles) {
}
