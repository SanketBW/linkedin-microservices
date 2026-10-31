package com.linkedin.post.controller;

import com.linkedin.post.dto.PageResponse;
import com.linkedin.post.dto.PostResponse;
import com.linkedin.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;

    @GetMapping
    public PageResponse<PostResponse> getFeed(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return postService.getAllPosts(pageable);
    }
}

