package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.QuestionEvent;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class SearchIndexEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexEventConsumer.class);

    private final SearchService searchService;

    public SearchIndexEventConsumer(SearchService searchService) {
        this.searchService = searchService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.stackoverflow-events:stackoverflow.events}",
            groupId = "${app.kafka.consumer-group.search:stackoverflow-search-indexer}"
    )
    public void consume(QuestionEvent event) {
        String questionId = event.getQuestionId() != null
                ? event.getQuestionId()
                : event.getEntityId();

        log.info("Search index consumer received Kafka event: type={} entityId={} questionId={} userId={} occurredAt={}",
                event.getEventType(),
                event.getEntityId(),
                event.getQuestionId(),
                event.getUserId(),
                event.getOccurredAt());

        try {
            switch (event.getEventType()) {
                case "QUESTION_CREATED" -> {
                    searchService.indexQuestion(questionId);
                }

                case "QUESTION_DELETED" -> {
                    searchService.deleteQuestion(questionId);
                }

                case "ANSWER_CREATED", "ANSWER_DELETED" -> {
                    searchService.indexQuestion(questionId);
                }

                case "VOTE_CREATED", "VOTE_UPDATED", "VOTE_DELETED" -> {
                    searchService.indexQuestion(questionId);
                }

                default -> log.warn("Unknown event type for search index: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Search index update failed for eventType={} questionId={}",
                    event.getEventType(), questionId, e);
        }
    }
}
