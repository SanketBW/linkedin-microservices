package com.linkedin.post.consumer;

import com.linkedin.post.event.UserDeletedEvent;
import com.linkedin.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {
    private final PostRepository postRepository;

    @KafkaListener(topics = "user-events", groupId = "post-service",
            properties = "spring.json.value.default.type=com.linkedin.post.event.UserDeletedEvent")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {

        if ("USER_DELETED".equals(event.eventType()))
            postRepository.deleteByUserId(event.userId());
    }
}
