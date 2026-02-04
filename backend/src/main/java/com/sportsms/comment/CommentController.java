package com.sportsms.comment;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    public Page<CommentDto.CommentResponse> list(@PathVariable UUID postId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return commentService.list(postId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(comment -> new CommentDto.CommentResponse(
                        comment.getId(),
                        comment.getPost().getId(),
                        comment.getAuthor() != null ? comment.getAuthor().getId() : null,
                        comment.getAuthorName(),
                        comment.getContent(),
                        comment.getCreatedAt()));
    }

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public CommentDto.CommentResponse add(@PathVariable UUID postId,
                                          @Valid @RequestBody CommentDto.CommentRequest request,
                                          Authentication authentication) {
        Comment comment = commentService.add(postId, request, authentication.getName());
        return new CommentDto.CommentResponse(comment.getId(), postId,
                comment.getAuthor() != null ? comment.getAuthor().getId() : null,
                comment.getAuthorName(), comment.getContent(), comment.getCreatedAt());
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN') or @commentSecurity.isOwner(authentication, #commentId)")
    public void delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
    }
}
