package com.linkedin.post.controller;

import com.linkedin.post.dto.CommentResponse;
import com.linkedin.post.dto.CreateCommentRequest;
import com.linkedin.post.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService commentService;

    @PostMapping
    public CommentResponse createComment(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request, Authentication auth)
    {
        Long userId = Long.valueOf(auth.getName());
        return commentService.createComment(postId, userId, request);
    }

    @GetMapping
    public Page<CommentResponse> getComments(@PathVariable Long postId, @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size)
    {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return commentService.getComments(postId, pageable);
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(@PathVariable Long postId, @PathVariable Long commentId,
                                         @Valid @RequestBody CreateCommentRequest request, Authentication auth)
    {
        Long userId = Long.valueOf(auth.getName());
        return commentService.updateComment(commentId, userId, request);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        commentService.deleteComment(commentId, userId);
    }
}

