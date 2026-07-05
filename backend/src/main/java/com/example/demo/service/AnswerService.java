package com.example.demo.service;

import java.util.List;

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

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public AnswerService(
            AnswerRepository answerRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository,
            KafkaEventProducer kafkaEventProducer
    ) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.kafkaEventProducer = kafkaEventProducer;
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
    kafkaEventProducer.publish(QuestionEvent.answerCreated(saved.getId(), questionId, user.getId()));
    return saved;
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
