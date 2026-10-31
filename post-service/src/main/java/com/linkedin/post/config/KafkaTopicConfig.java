package com.linkedin.post.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic postEventsTopic() {
        return TopicBuilder.name("post-events").partitions(3).replicas(1)        // one broker in dev
                .build();
    }

    @Bean
    public NewTopic postLikeEventsTopic() {
        return TopicBuilder.name("post-like-events").partitions(3).replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentEventsTopic() {
        return TopicBuilder.name("comment-events").partitions(3).replicas(1)
                .build();
    }

}