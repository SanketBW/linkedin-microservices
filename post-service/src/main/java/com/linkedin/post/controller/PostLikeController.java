package com.linkedin.post.controller;

import com.linkedin.post.dto.PostLikeResponse;
import com.linkedin.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public PostLikeResponse likePost(@PathVariable Long postId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return postLikeService.likePost(postId, userId);
    }

    @DeleteMapping
    public PostLikeResponse unlikePost(@PathVariable Long postId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return postLikeService.unlikePost(postId, userId);
    }

    @GetMapping
    public PostLikeResponse getLikeStatus(@PathVariable Long postId, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return postLikeService.getLikeStatus(postId, userId);
    }
}

