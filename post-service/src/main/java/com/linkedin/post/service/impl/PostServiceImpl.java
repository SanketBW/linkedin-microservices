package com.linkedin.post.service.impl;

import com.linkedin.post.dto.CreatePostRequest;
import com.linkedin.post.dto.PageResponse;
import com.linkedin.post.dto.PostResponse;
import com.linkedin.post.dto.UpdatePostRequest;
import com.linkedin.post.entity.Post;
import com.linkedin.post.event.PostCreatedEvent;
import com.linkedin.post.producer.PostEventProducer;
import com.linkedin.post.exception.ResourceNotFoundException;
import com.linkedin.post.repository.PostRepository;
import com.linkedin.post.service.PostService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostEventProducer postEventProducer;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(value = "postFeed", allEntries = true)
    public PostResponse createPost(Long userId, CreatePostRequest request) {

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(request.getContent().trim());
        Post saved = postRepository.save(post);

        // Create Kafka event
        PostCreatedEvent event = new PostCreatedEvent(
                UUID.randomUUID().toString(),
                saved.getId(),
                saved.getUserId(),
                saved.getCreatedAt()
        );
        postEventProducer.publishPostCreated(event);
        return modelMapper.map(saved, PostResponse.class);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#postId")
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return modelMapper.map(post, PostResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "postFeed",
            key = "#pageable.pageNumber + '-' + " + "T(java.lang.Math).min(#pageable.pageSize, 50)")
    public PageResponse<PostResponse> getAllPosts(Pageable pageable) {
        Pageable safePageable = newestFirst(pageable);
        Page<PostResponse> posts = postRepository
                .findAll(safePageable).map((post)-> modelMapper.map(post, PostResponse.class));
        return PageResponse.from(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(Long userId, Pageable pageable) {
        return postRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map((post)-> modelMapper.map(post, PostResponse.class));
    }

    @Override
    @CacheEvict(value = {"posts", "postFeed"}, allEntries = true)
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        checkOwnership(post, userId);
        post.setContent(request.getContent().trim());
        post.setImageKey(request.getImageKey());
        Post updatedPost = postRepository.save(post);
        return modelMapper.map(updatedPost, PostResponse.class);
    }

    @Override
    @CacheEvict(value = {"posts", "postFeed"}, allEntries = true)
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        checkOwnership(post, userId);
        postRepository.delete(post);
    }

    private Pageable newestFirst(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private void checkOwnership(Post post, Long userId) {
        if (!post.getUserId().equals(userId))
            throw new AccessDeniedException("You are not allowed to modify this post");
    }
}
