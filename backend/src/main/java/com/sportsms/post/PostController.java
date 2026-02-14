package com.sportsms.post;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/published")
    public Page<PostDto.PostResponse> listPublished(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Post> posts = postService.searchPublished(keyword, startDate, endDate,
                PageRequest.of(page, size));
        return posts.map(this::toResponse);
    }

    @GetMapping("/published/{id}")
    public PostDto.PostResponse getPublished(@PathVariable("id") UUID id) {
        return toResponse(postService.getPublished(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PostDto.PostResponse> listAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Post> posts = postService.searchAll(keyword, status,
                PageRequest.of(page, size));
        return posts.map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PostDto.PostResponse create(@Valid @RequestBody PostDto.PostRequest request, Authentication authentication) {
        Post post = postService.create(request, authentication.getName());
        return toResponse(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PostDto.PostResponse update(@PathVariable("id") UUID id, @Valid @RequestBody PostDto.PostRequest request) {
        return toResponse(postService.update(id, request));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public PostDto.PostResponse publish(@PathVariable("id") UUID id, @RequestParam(name = "publish") boolean publish) {
        return toResponse(postService.publish(id, publish));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") UUID id) {
        postService.delete(id);
    }

    private PostDto.PostResponse toResponse(Post post) {
        return new PostDto.PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCoverImageUrl(),
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getFullName() : null,
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }
}
