package com.linkedin.notification.event;

import com.linkedin.notification.enums.ConnectionEventType;

import java.time.LocalDateTime;

public record ConnectionEvent(String eventId,
                              ConnectionEventType eventType,
                              Long connectionId,
                              Long actorId,
                              Long recipientId,
                              LocalDateTime createdAt
) { }