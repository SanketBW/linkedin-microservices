package com.linkedin.connection.consumer;

import com.linkedin.connection.event.UserDeletedEvent;
import com.linkedin.connection.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {
    private final ConnectionRepository connectionRepository;

    @KafkaListener(topics = "user-events", groupId = "connection-service",
    properties = "spring.json.value.default.type=com.linkedin.connection.event.UserDeletedEvent")
    public void handleUserDeleted(UserDeletedEvent event) {

        if ("USER_DELETED".equals(event.eventType()))
            connectionRepository.deleteBySenderIdOrReceiverId(event.userId(), event.userId());
    }
}
