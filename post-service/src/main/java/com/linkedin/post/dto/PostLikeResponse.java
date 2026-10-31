package com.linkedin.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostLikeResponse {

    private Long postId;
    private long likeCount;
    private boolean likedByCurrentUser;
}

