package com.linkedin.connection.event;

import com.linkedin.connection.enums.ConnectionEventType;

import java.time.LocalDateTime;

public record ConnectionEvent(
        String eventId,
        ConnectionEventType eventType,
        Long connectionId,
        Long actorId,
        Long recipientId,
        LocalDateTime createdAt
) {}