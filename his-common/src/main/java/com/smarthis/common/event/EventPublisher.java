package com.smarthis.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, HisEvent<?> event) {
        String key = event.getEventId();
        log.info("Publishing event: topic={}, type={}, eventId={}", topic, event.getEventType(), event.getEventId());
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event: topic={}, eventId={}", topic, event.getEventId(), ex);
                    } else {
                        log.debug("Event published: topic={}, partition={}, offset={}",
                                topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}
