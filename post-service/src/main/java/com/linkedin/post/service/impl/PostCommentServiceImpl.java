package com.linkedin.post.service.impl;

import com.linkedin.post.dto.CommentResponse;
import com.linkedin.post.dto.CreateCommentRequest;
import com.linkedin.post.entity.Post;
import com.linkedin.post.entity.PostComment;
import com.linkedin.post.event.CommentCreatedEvent;
import com.linkedin.post.producer.PostEventProducer;
import com.linkedin.post.repository.PostCommentRepository;
import com.linkedin.post.exception.ResourceNotFoundException;
import com.linkedin.post.repository.PostRepository;
import com.linkedin.post.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentServiceImpl implements PostCommentService {
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final PostEventProducer postEventProducer;

    @Override
    @CacheEvict(value = "postCommentCounts", key = "#postId")
    public CommentResponse createComment(Long postId, Long userId, CreateCommentRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        PostComment savedComment = commentRepository.save(comment);

        if (!post.getUserId().equals(userId)) {
            // Create Kafka event
            CommentCreatedEvent event = new CommentCreatedEvent(UUID.randomUUID().toString(),
                    savedComment.getId(), postId, userId, post.getUserId(), LocalDateTime.now()
            );
            // Publish event
            postEventProducer.publishCommentCreated(event);
        }


        return modelMapper.map(savedComment, CommentResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return commentRepository
                .findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(comment -> modelMapper.map(comment, CommentResponse.class));
    }

    @Override
    @CacheEvict(value = "postCommentCounts", allEntries = true)
    public CommentResponse updateComment(Long commentId, Long userId, CreateCommentRequest request) {

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        checkOwnership(comment, userId);
        comment.setContent(request.getContent().trim());
        PostComment updatedComment = commentRepository.save(comment);
        return modelMapper.map(updatedComment, CommentResponse.class);
    }

    @Override
    @CacheEvict(value = "postCommentCounts", allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        checkOwnership(comment, userId);
        commentRepository.delete(comment);
    }

    private void checkOwnership(PostComment comment, Long userId) {
        if (!comment.getUserId().equals(userId))
            throw new AccessDeniedException("You are not allowed to modify this comment");
    }
}

