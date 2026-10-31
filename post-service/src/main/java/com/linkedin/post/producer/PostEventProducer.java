package com.linkedin.post.producer;

import com.linkedin.post.event.CommentCreatedEvent;
import com.linkedin.post.event.KafkaTopics;
import com.linkedin.post.event.PostCreatedEvent;
import com.linkedin.post.event.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPostCreated(PostCreatedEvent event) {
        send(KafkaTopics.POST_EVENTS, event.postId(), event.eventId(), event);
    }
    public void publishPostLiked(PostLikedEvent event) {
        send(KafkaTopics.POST_LIKE_EVENTS, event.postId(), event.eventId(), event);
    }
    public void publishCommentCreated(CommentCreatedEvent event) {
        send(KafkaTopics.COMMENT_EVENTS, event.postId(), event.eventId(), event);
    }

    private void send(String topic, Long key, String eventId, Object event) {

        kafkaTemplate.send(topic, key.toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null)
                    {
                        log.error("Kafka publish failed: topic={}, eventId={}", topic, eventId, exception);
                    }
                    else
                    {
                        log.info("Kafka event published: topic={}, eventId={}, partition={}",
                                topic, eventId, result.getRecordMetadata().partition());
                    }
                });
    }
}