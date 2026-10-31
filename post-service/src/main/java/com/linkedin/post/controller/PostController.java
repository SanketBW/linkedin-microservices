package com.linkedin.post.controller;

import com.linkedin.post.dto.CreatePostRequest;
import com.linkedin.post.dto.PageResponse;
import com.linkedin.post.dto.PostResponse;
import com.linkedin.post.dto.UpdatePostRequest;
import com.linkedin.post.payload.ApiResponse;
import com.linkedin.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(Pageable pageable)
    {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostResponse>> getMyPosts(Authentication auth, Pageable pageable)
    {
        Long userId = Long.valueOf(auth.getName());
        Page<PostResponse> response = postService.getMyPosts(userId, pageable);
        return ResponseEntity.ok(response);
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