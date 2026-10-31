package com.linkedin.notification.consumer;

import com.linkedin.notification.enums.NotificationType;
import com.linkedin.notification.event.CommentCreatedEvent;
import com.linkedin.notification.event.PostCreatedEvent;
import com.linkedin.notification.event.PostLikedEvent;
import com.linkedin.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "post-events", groupId = "notification-group",
            properties = "spring.json.value.default.type=com.linkedin.notification.event.PostCreatedEvent")
    public void consumePostCreated(PostCreatedEvent event) {
        log.info("Received PostCreatedEvent: postId={}, authorId={}", event.postId(), event.authorId());
    }

    @KafkaListener(topics = "post-like-events", groupId = "notification-group",
            properties = "spring.json.value.default.type=com.linkedin.notification.event.PostLikedEvent")
    public void consumePostLiked(PostLikedEvent event) {
        log.info("Received PostLikedEvent: postId={}, actorId={}", event.postId(), event.actorId());

        notificationService.record(
                event.eventId(),
                event.recipientId(),
                event.actorId(),
                NotificationType.POST_LIKED,
                event.postId(),
                event.createdAt());
    }

    @KafkaListener(topics = "comment-events", groupId = "notification-group",
            properties = "spring.json.value.default.type=com.linkedin.notification.event.CommentCreatedEvent")
    public void consumeCommentCreated(CommentCreatedEvent event) {

        log.info("Received CommentCreatedEvent: commentId={}, postId={}", event.commentId(), event.postId());

        notificationService.record(
                event.eventId(),
                event.recipientId(),
                event.actorId(),
                NotificationType.POST_COMMENTED,
                event.postId(),
                event.createdAt());
    }
}