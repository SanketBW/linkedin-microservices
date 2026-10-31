package com.linkedin.connection.producer;

import com.linkedin.connection.config.KafkaTopicConfig;
import com.linkedin.connection.event.ConnectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(ConnectionEvent event) {

        kafkaTemplate.send(
                KafkaTopicConfig.CONNECTION_EVENTS,
                event.connectionId().toString(), event)
                .whenComplete((result, ex) -> {

            if (ex != null)
            {
                log.error("Failed to publish ConnectionEvent: eventId={}", event.eventId(), ex);
            }
            else {
                log.info("ConnectionEvent published: type={}, eventId={}, partition={}",
                        event.eventType(), event.eventId(), result.getRecordMetadata().partition());
            }
        });
    }
}
