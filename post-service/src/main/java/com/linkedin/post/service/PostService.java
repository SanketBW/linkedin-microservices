
package com.linkedin.post.service;

import com.linkedin.post.dto.CreatePostRequest;
import com.linkedin.post.dto.PageResponse;
import com.linkedin.post.dto.PostResponse;
import com.linkedin.post.dto.UpdatePostRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createPost(Long userId, CreatePostRequest request);
    PostResponse getPostById(Long postId);
    PageResponse<PostResponse> getAllPosts(Pageable pageable);
    Page<PostResponse> getMyPosts(Long userId, Pageable pageable);
    PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);
    void deletePost(Long postId, Long userId);
}
