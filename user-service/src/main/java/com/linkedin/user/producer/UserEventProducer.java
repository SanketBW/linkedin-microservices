package com.linkedin.user.producer;

import com.linkedin.user.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    public void publishUserDeleted(Long userId) {

        UserDeletedEvent event = new UserDeletedEvent(
                UUID.randomUUID().toString(), userId,
                "USER_DELETED", LocalDateTime.now());

        kafkaTemplate.send("user-events", String.valueOf(userId), event);
    }
}
