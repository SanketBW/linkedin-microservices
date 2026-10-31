package com.linkedin.notification.consumer;

import com.linkedin.notification.enums.NotificationType;
import com.linkedin.notification.event.ConnectionEvent;
import com.linkedin.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "connection-events", groupId = "notification-group",
            properties = "spring.json.value.default.type=com.linkedin.notification.event.ConnectionEvent")
    public void consumeConnectionEvent(ConnectionEvent event) {
        log.info("Received connection event: eventId={}, type={}, connectionId={}, actorId={}, recipientId={}",
                event.eventId(), event.eventType(), event.connectionId(), event.actorId(), event.recipientId());

        if (event.eventType() == null) {
            log.warn("Ignoring connection event with no eventType: eventId={}", event.eventId());
            return;
        }

        NotificationType type = switch (event.eventType()) {
            case REQUESTED -> NotificationType.CONNECTION_REQUESTED;
            case ACCEPTED -> NotificationType.CONNECTION_ACCEPTED;
        };

        notificationService.record(
                event.eventId(),
                event.recipientId(),
                event.actorId(),
                type,
                event.connectionId(),
                event.createdAt());
    }
}