package com.linkedin.connection.entity;

import com.linkedin.connection.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_connection_sender_receiver",
                        columnNames = {"sender_id", "receiver_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_connection_sender_status",
                        columnList = "sender_id, status"
                ),
                @Index(
                        name = "idx_connection_receiver_status",
                        columnList = "receiver_id, status"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = ConnectionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
