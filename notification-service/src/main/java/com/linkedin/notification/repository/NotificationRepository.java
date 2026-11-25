package com.linkedin.notification.repository;

import com.linkedin.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByEventId(String eventId);

    void deleteByRecipientIdOrActorId(Long recipientId, Long actorId);

    Page<Notification> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long notificationId, Long recipientId);
}