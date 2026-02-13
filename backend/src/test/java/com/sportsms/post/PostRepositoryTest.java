package com.sportsms.post;

import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void searchPublishedFindsMatchesInTextContent() {
        Post post = new Post();
        post.setTitle("League report");
        post.setContent("This recap includes a golden goal in extra time");
        post.setStatus(PostStatus.PUBLISHED);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        Page<Post> results = postRepository.searchPublished("golden", null, null, PageRequest.of(0, 10));

        Assertions.assertEquals(1, results.getTotalElements());
        Assertions.assertEquals("League report", results.getContent().get(0).getTitle());
    }
}
