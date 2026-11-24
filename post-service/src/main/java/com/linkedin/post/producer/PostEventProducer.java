package com.linkedin.post.producer;

import com.linkedin.post.event.CommentCreatedEvent;
import com.linkedin.post.event.KafkaTopics;
import com.linkedin.post.event.PostCreatedEvent;
import com.linkedin.post.event.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPostCreated(PostCreatedEvent event){
        kafkaTemplate.send(KafkaTopics.POST_EVENTS, event.postId().toString(), event);
        log.info("Post created event published for postId: {}", event.postId());
    }

    public void publishPostLiked(PostLikedEvent event){
        kafkaTemplate.send(KafkaTopics.POST_LIKE_EVENTS, event.postId().toString(), event);
        log.info("Post Liked event published for postId: {}", event.postId());
    }

    public void publishCommentCreated(CommentCreatedEvent event){
        kafkaTemplate.send(KafkaTopics.COMMENT_EVENTS, event.postId().toString(), event);
        log.info("Post comment event published for postId: {}", event.postId());
    }

}