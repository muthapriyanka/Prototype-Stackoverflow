package com.example.demo.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Answer;
import com.example.demo.Comment;
import com.example.demo.CommentDTO;
import com.example.demo.CreateCommentRequest;
import com.example.demo.Question;
import com.example.demo.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.QuestionRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public CommentService(
            CommentRepository commentRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository
    ) {
        this.commentRepository = commentRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional
    @CacheEvict(value = { "questionResponsesById", "questionDetails" }, allEntries = true)
    public CommentDTO createComment(CreateCommentRequest request, User user) {
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("Comment body is required");
        }

        boolean hasQuestionId = request.getQuestionId() != null && !request.getQuestionId().isBlank();
        boolean hasAnswerId = request.getAnswerId() != null && !request.getAnswerId().isBlank();
        if (hasQuestionId == hasAnswerId) {
            throw new IllegalArgumentException("Comment must belong to exactly one question or answer");
        }

        Comment comment = new Comment();
        comment.setBody(request.getBody().trim());
        comment.setUser(user);

        if (hasQuestionId) {
            Question question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            comment.setQuestion(question);
        } else {
            Answer answer = answerRepository.findById(request.getAnswerId())
                    .orElseThrow(() -> new RuntimeException("Answer not found"));
            comment.setAnswer(answer);
        }

        Comment saved = commentRepository.saveAndFlush(comment);
        return toDTO(saved);
    }

    private CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getBody(),
                comment.getUser() != null ? comment.getUser().getUsername() : "Unknown",
                comment.getCreatedAt()
        );
    }
}
