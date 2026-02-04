package com.sportsms.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class PostDto {
    public record PostResponse(UUID id, String title, String content, String coverImageUrl,
                               UUID authorId, String authorName, PostStatus status,
                               Instant createdAt, Instant updatedAt) {}

    public record PostRequest(@NotBlank String title,
                              @NotBlank String content,
                              String coverImageUrl,
                              @NotNull PostStatus status) {}
}
