package com.sportsms.comment;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("commentSecurity")
public class CommentSecurity {
    private final CommentService commentService;

    public CommentSecurity(CommentService commentService) {
        this.commentService = commentService;
    }

    public boolean isOwner(Authentication authentication, java.util.UUID commentId) {
        Comment comment = commentService.get(commentId);
        return comment.getAuthor() != null && comment.getAuthor().getEmail().equals(authentication.getName());
    }
}
