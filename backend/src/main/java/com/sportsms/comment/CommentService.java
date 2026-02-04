package com.sportsms.comment;

import com.sportsms.common.NotFoundException;
import com.sportsms.post.Post;
import com.sportsms.post.PostRepository;
import com.sportsms.user.User;
import com.sportsms.user.UserRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Page<Comment> list(UUID postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    public Comment add(UUID postId, CommentDto.CommentRequest request, String authorEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setAuthorName(author.getFullName());
        comment.setContent(request.content());
        return commentRepository.save(comment);
    }

    public void delete(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }

    public Comment get(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }
}
