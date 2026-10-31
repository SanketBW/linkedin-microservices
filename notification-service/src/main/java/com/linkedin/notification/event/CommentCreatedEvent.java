package com.linkedin.notification.event;

import java.time.LocalDateTime;

public record CommentCreatedEvent(
        String eventId,
        Long commentId,
        Long postId,
        Long actorId,
        Long recipientId,
        LocalDateTime createdAt
) { }
