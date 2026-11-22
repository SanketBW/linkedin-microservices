package com.linkedin.post.controller;

import com.linkedin.post.dto.CreatePostRequest;
import com.linkedin.post.dto.PageResponse;
import com.linkedin.post.dto.PostResponse;
import com.linkedin.post.dto.UpdatePostRequest;
import com.linkedin.post.payload.ApiResponse;
import com.linkedin.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(Authentication auth,
            @Valid @RequestBody CreatePostRequest request) {
        Long userId = Long.valueOf(auth.getName());
        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }

    @GetMapping("/feed/me")
    public ResponseEntity<PageResponse<PostResponse>> getMyPosts(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = Long.valueOf(auth.getName());
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, Authentication auth,
                                         @Valid @RequestBody UpdatePostRequest request)
    {
        Long userId = Long.valueOf(auth.getName());
        PostResponse response = postService.updatePost(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Long id, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        postService.deletePost(id, userId);
        ApiResponse response = new ApiResponse("Post deleted successfully!", true);
        return ResponseEntity.ok(response);
    }

}