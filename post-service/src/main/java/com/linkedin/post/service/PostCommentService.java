package com.linkedin.post.service;

import com.linkedin.post.dto.CommentResponse;
import com.linkedin.post.dto.CreateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface PostCommentService {

    CommentResponse createComment(Long postId, Long userId, CreateCommentRequest request);

    Page<CommentResponse> getComments(Long postId, Pageable pageable);

    CommentResponse updateComment(Long commentId, Long userId, CreateCommentRequest request);

    void deleteComment(Long commentId, Long userId);
}
