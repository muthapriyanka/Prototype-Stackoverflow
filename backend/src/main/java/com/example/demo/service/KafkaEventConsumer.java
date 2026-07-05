package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.QuestionEvent;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final FeedService feedService;

    public KafkaEventConsumer(FeedService feedService) {
        this.feedService = feedService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.stackoverflow-events:stackoverflow.events}",
            groupId = "${app.kafka.consumer-group.feed:stackoverflow-feed-local}"
    )
    public void consume(QuestionEvent event) {
        String questionId = event.getQuestionId() != null
            ? event.getQuestionId()
            : event.getEntityId();

        log.info("Feed consumer received Kafka event: type={} entityId={} questionId={} userId={} occurredAt={}",
                event.getEventType(),
                event.getEntityId(),
                event.getQuestionId(),
                event.getUserId(),
                event.getOccurredAt());

        try {
            switch (event.getEventType()) {
                case "QUESTION_CREATED" -> {
                    feedService.addQuestionToFeed(questionId);
                }

                case "QUESTION_DELETED" -> {
                    feedService.removeQuestionFromFeed(questionId);
                }

                case "ANSWER_CREATED", "ANSWER_DELETED" -> {
                    feedService.refreshQuestionInFeed(questionId);
                }

                case "VOTE_CREATED", "VOTE_UPDATED", "VOTE_DELETED" -> {
                    feedService.refreshQuestionScore(questionId);
                }

                default -> log.warn("Unknown Kafka event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Feed update failed for eventType={} questionId={}", event.getEventType(), questionId, e);
        }
    }
}
