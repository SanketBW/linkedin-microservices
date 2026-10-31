package com.linkedin.post.controller;


import com.linkedin.post.repository.PostRepository;
import com.linkedin.post.exception.ResourceNotFoundException;
import com.linkedin.post.service.impl.PostCommentCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentCountController {

    private final PostCommentCountService commentCountService;
    private final PostRepository postRepository;

    @GetMapping("/count")
    public Map<String, Object> getCommentCount(@PathVariable Long postId) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        return Map.of("postId", postId, "commentCount", commentCountService.getCommentCount(postId));
    }
}

