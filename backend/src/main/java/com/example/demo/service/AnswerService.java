package com.example.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Answer;
import com.example.demo.QuestionEvent;
import com.example.demo.Question;
import com.example.demo.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerService.class);

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final FeedService feedService;
    private final SearchService searchService;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.answer.create-processing-mode:kafka}")
    private String createProcessingMode;

    public AnswerService(
            AnswerRepository answerRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository,
            KafkaEventProducer kafkaEventProducer,
            FeedService feedService,
            SearchService searchService,
            EmailNotificationService emailNotificationService
    ) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.kafkaEventProducer = kafkaEventProducer;
        this.feedService = feedService;
        this.searchService = searchService;
        this.emailNotificationService = emailNotificationService;
    }

    @CacheEvict(value = { "questions", "questionResponses", "questionResponsesById", "questionDetails" }, allEntries = true)
    public Answer createAnswer(String body, String questionId, User user) {

    // Find question
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found"));

    Answer answer = new Answer();
    answer.setBody(body);
    answer.setQuestion(question);
    answer.setUser(user);  // ✅ Use the logged-in user
    answer.setAccepted(false);

    Answer saved = answerRepository.save(answer);
    handleAnswerCreated(saved, questionId, user);
    return saved;
}

    private void handleAnswerCreated(Answer answer, String questionId, User user) {
        String mode = createProcessingMode == null
                ? "kafka"
                : createProcessingMode.trim().toLowerCase();

        switch (mode) {
            case "sync" -> {
                feedService.refreshQuestionInFeed(questionId);
                searchService.indexQuestion(questionId);
                emailNotificationService.notifyQuestionOwnerAboutAnswer(answer.getId(), questionId, user.getId());
            }
            case "none" -> log.info("Answer create post-processing disabled for answerId={} questionId={}",
                    answer.getId(), questionId);
            case "kafka" -> kafkaEventProducer.publish(
                    QuestionEvent.answerCreated(answer.getId(), questionId, user.getId())
            );
            default -> {
                log.warn("Unknown answer create processing mode '{}'. Falling back to Kafka.", createProcessingMode);
                kafkaEventProducer.publish(
                        QuestionEvent.answerCreated(answer.getId(), questionId, user.getId())
                );
            }
        }
    }


    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Answer> getAnswersByQuestionId(String questionId) {
    return answerRepository.findByQuestion_Id(questionId);
    }

    @CacheEvict(value = { "questions", "questionResponses", "questionResponsesById", "questionDetails" }, allEntries = true)
    @Transactional
    public void deleteAnswer(String id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        String questionId = answer.getQuestion() != null ? answer.getQuestion().getId() : null;
        String userId = answer.getUser() != null ? answer.getUser().getId() : null;

        answerRepository.delete(answer);
        kafkaEventProducer.publish(QuestionEvent.answerDeleted(id, questionId, userId));
    }
}
