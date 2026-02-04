package com.sportsms.comment;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class CommentDto {
    public record CommentResponse(UUID id, UUID postId, UUID authorId, String authorName, String content, Instant createdAt) {}

    public record CommentRequest(@NotBlank String content) {}
}
