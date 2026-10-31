package com.linkedin.post.event;

import java.time.LocalDateTime;

public record PostCreatedEvent(String eventId, Long postId, Long authorId, LocalDateTime createdAt)
{
}