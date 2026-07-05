package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.QuestionEvent;

@Service
public class KafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducer.class);

    private final KafkaTemplate<String, QuestionEvent> kafkaTemplate;
    private final boolean enabled;
    private final String topic;

    public KafkaEventProducer(
            KafkaTemplate<String, QuestionEvent> kafkaTemplate,
            @Value("${app.kafka.enabled:true}") boolean enabled,
            @Value("${app.kafka.topic.stackoverflow-events:stackoverflow.events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void publish(QuestionEvent event) {
        if (!enabled) {
            log.debug("Kafka publishing disabled. Skipping event {}", event.getEventType());
            return;
        }

        try {
            log.info("Kafka publishing event: type={} topic={} entityId={} questionId={}",
                    event.getEventType(), topic, event.getEntityId(), event.getQuestionId());

            kafkaTemplate.send(topic, event.getQuestionId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Kafka publish failed for event {} and entity {}",
                                    event.getEventType(), event.getEntityId(), ex);
                            return;
                        }

                        log.info("Kafka event published: type={} topic={} partition={} offset={}",
                                event.getEventType(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    });
        } catch (RuntimeException ex) {
            log.warn("Kafka publish skipped for event {} and entity {}",
                    event.getEventType(), event.getEntityId(), ex);
        }
    }
}
