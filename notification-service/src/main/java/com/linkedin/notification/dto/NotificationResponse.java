package com.linkedin.notification.dto;

import com.linkedin.notification.enums.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {
    private Long id;
    private String eventId;
    private Long recipientId;
    private Long actorId;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private Long referenceId;
    private boolean read = false;
    private LocalDateTime createdAt;
}
