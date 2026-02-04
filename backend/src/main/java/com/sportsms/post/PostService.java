package com.sportsms.post;

import com.sportsms.common.NotFoundException;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Page<Post> searchPublished(String keyword, Instant from, Instant to, Pageable pageable) {
        return postRepository.searchPublished(keyword, from, to, pageable);
    }

    public Page<Post> searchAll(String keyword, PostStatus status, Pageable pageable) {
        return postRepository.searchAll(keyword, status, pageable);
    }

    public Post getPublished(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new NotFoundException("Post not found");
        }
        return post;
    }

    public Post getById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
    }

    public Post create(PostDto.PostRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("Author not found"));
        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCoverImageUrl(request.coverImageUrl());
        post.setAuthor(author);
        post.setStatus(request.status());
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        return postRepository.save(post);
    }

    public Post update(UUID id, PostDto.PostRequest request) {
        Post post = getById(id);
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setCoverImageUrl(request.coverImageUrl());
        post.setStatus(request.status());
        post.setUpdatedAt(Instant.now());
        return postRepository.save(post);
    }

    public Post publish(UUID id, boolean publish) {
        Post post = getById(id);
        post.setStatus(publish ? PostStatus.PUBLISHED : PostStatus.DRAFT);
        post.setUpdatedAt(Instant.now());
        return postRepository.save(post);
    }

    public void delete(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }
}
