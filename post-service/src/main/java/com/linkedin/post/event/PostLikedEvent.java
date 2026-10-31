package com.linkedin.post.event;

import java.time.LocalDateTime;

public record PostLikedEvent(
        String eventId,
        Long postId,
        Long actorId,
        Long recipientId,
        LocalDateTime createdAt
) {}