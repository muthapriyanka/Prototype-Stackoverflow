package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.QuestionEvent;

@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AnswerNotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnswerNotificationEventConsumer.class);

    private final EmailNotificationService emailNotificationService;

    public AnswerNotificationEventConsumer(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.stackoverflow-events:stackoverflow.events}",
            groupId = "${app.kafka.consumer-group.notifications:stackoverflow-notifications-local}"
    )
    public void consume(QuestionEvent event) {
        if (!"ANSWER_CREATED".equals(event.getEventType())) {
            return;
        }

        log.info("Notification consumer received Kafka event: type={} answerId={} questionId={} userId={}",
                event.getEventType(),
                event.getEntityId(),
                event.getQuestionId(),
                event.getUserId());

        try {
            emailNotificationService.notifyQuestionOwnerAboutAnswer(
                    event.getEntityId(),
                    event.getQuestionId(),
                    event.getUserId()
            );
        } catch (Exception ex) {
            log.error("Answer notification failed for answerId={} questionId={}",
                    event.getEntityId(),
                    event.getQuestionId(),
                    ex);
        }
    }
}
