package com.linkedin.post.repository;

import com.linkedin.post.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Page<PostComment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    Page<PostComment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByPostId(Long postId);
}

