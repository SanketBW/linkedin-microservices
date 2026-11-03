package com.linkedin.notification.consumer;

import com.linkedin.notification.event.UserDeletedEvent;
import com.linkedin.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-events", groupId = "notification-service",
            properties = "spring.json.value.default.type=com.linkedin.notification.event.UserDeletedEvent")
    public void handleUserDeleted(UserDeletedEvent event) {

        if (!"USER_DELETED".equals(event.eventType())) return;

        log.info("Received UserDeletedEvent for userId={}", event.userId());
        notificationService.deleteAllNotificationsOf(event.userId());
    }
}
