package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.Answer;
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

    public AnswerService(
            AnswerRepository answerRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository
    ) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public Answer createAnswer(String body, String questionId, User user) {

    // Find question
    Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found"));

    Answer answer = new Answer();
    answer.setBody(body);
    answer.setQuestion(question);
    answer.setUser(user);  // ✅ Use the logged-in user
    answer.setAccepted(false);

    return answerRepository.save(answer);
}


    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Answer> getAnswersByQuestionId(String questionId) {
    return answerRepository.findByQuestion_Id(questionId);
    }

    public void deleteAnswer(String id) {
        answerRepository.deleteById(id);
    }
}
