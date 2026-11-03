package com.linkedin.connection.event;

import java.time.LocalDateTime;

public record UserDeletedEvent(
        String eventId,
        Long userId,
        String eventType,
        LocalDateTime createdAt
) {}