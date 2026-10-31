package com.linkedin.notification.entity;

import com.linkedin.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        uniqueConstraints = {@UniqueConstraint(name = "uk_notification_event", columnNames = "event_id")},
        indexes = {@Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at")})
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false, length = 64)
    private String eventId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;                          // whose post is this(user_id)

    @Column(name = "actor_id", nullable = false)
    private Long actorId;                       // sender_id(user_id)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "reference_id")
    private Long referenceId;               // post_id

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
