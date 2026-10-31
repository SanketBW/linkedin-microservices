package com.linkedin.notification.service;

import com.linkedin.notification.entity.Notification;
import com.linkedin.notification.enums.NotificationType;
import com.linkedin.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository repository;

    public void record(String eventId, Long recipientId, Long actorId, NotificationType type, Long referenceId, LocalDateTime createdAt)
    {
        if (eventId == null || recipientId == null || actorId == null || type == null) {
            throw new IllegalArgumentException("Invalid notification event data");
        }

        if (recipientId.equals(actorId)) {
            return;
        }

        // Prevent duplicate notifications
        if (repository.existsByEventId(eventId)) {
            log.info("Duplicate event ignored: {}", eventId);
            return;
        }

        Notification notification = new Notification();

        notification.setEventId(eventId);
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(createdAt);

        repository.save(notification);

        log.info("Notification saved: type={}, recipientId={}", type, recipientId);
    }

    // TODO: Implement JWT, API's to retrieve notifications, (mark individual/all notifications as read) & more


}