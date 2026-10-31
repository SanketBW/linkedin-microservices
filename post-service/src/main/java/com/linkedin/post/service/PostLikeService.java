package com.linkedin.post.service;

import com.linkedin.post.dto.PostLikeResponse;

public interface PostLikeService {

    PostLikeResponse likePost(Long postId, Long userId);

    PostLikeResponse unlikePost(Long postId, Long userId);

    PostLikeResponse getLikeStatus(Long postId, Long userId);
}

