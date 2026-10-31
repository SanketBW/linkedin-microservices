package com.linkedin.post.service.impl;


import com.linkedin.post.dto.PostLikeResponse;
import com.linkedin.post.entity.Post;
import com.linkedin.post.entity.PostLike;
import com.linkedin.post.producer.PostEventProducer;
import com.linkedin.post.event.PostLikedEvent;
import com.linkedin.post.exception.ResourceNotFoundException;
import com.linkedin.post.repository.PostLikeRepository;
import com.linkedin.post.repository.PostRepository;
import com.linkedin.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;

    @Override
    @CacheEvict(value = "postLikeCounts", key = "#postId")
    public PostLikeResponse likePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Post not found with id: " + postId));
        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeRepository.saveAndFlush(like);

            if (!post.getUserId().equals(userId)) {
                PostLikedEvent event = new PostLikedEvent(UUID.randomUUID().toString(),
                        postId, userId, post.getUserId(), LocalDateTime.now());

                // 5. Publish event to Kafka
                postEventProducer.publishPostLiked(event);
            }
        }
        return getLikeStatus(postId, userId);
    }

    @Override
    @CacheEvict(value = "postLikeCounts", key = "#postId")
    public PostLikeResponse unlikePost(Long postId, Long userId) {

        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (like != null)
            postLikeRepository.delete(like);

        return getLikeStatus(postId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostLikeResponse getLikeStatus(Long postId, Long userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        long likeCount = getCachedLikeCount(postId);
        boolean likedByCurrentUser = postLikeRepository.existsByPostIdAndUserId(postId, userId);
        return new PostLikeResponse(postId, likeCount, likedByCurrentUser);
    }

    @Cacheable(value = "postLikeCounts", key = "#postId")
    public long getCachedLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}

