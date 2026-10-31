package com.linkedin.post.service.impl;

import com.linkedin.post.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentCountService {

    private final PostCommentRepository commentRepository;

    @Cacheable(value = "postCommentCounts", key = "#postId")
    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }
}

