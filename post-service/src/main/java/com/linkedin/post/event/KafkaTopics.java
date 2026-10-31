package com.linkedin.post.event;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String POST_EVENTS = "post-events";
    public static final String POST_LIKE_EVENTS = "post-like-events";
    public static final String COMMENT_EVENTS = "comment-events";
}
