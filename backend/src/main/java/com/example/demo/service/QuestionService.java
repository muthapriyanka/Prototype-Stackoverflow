package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Answer;
import com.example.demo.AnswerDTO;
import com.example.demo.Comment;
import com.example.demo.CommentDTO;
import com.example.demo.Question;
import com.example.demo.QuestionDetailResponse;
import com.example.demo.QuestionEvent;
import com.example.demo.QuestionRequest;
import com.example.demo.QuestionResponse;
import com.example.demo.QuestionWithAnswersDTO;
import com.example.demo.Tag;
import com.example.demo.TagDTO;
import com.example.demo.Vote;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final KafkaEventProducer kafkaEventProducer;

    // No cache here: list/feed can change often
    public List<QuestionWithAnswersDTO> getAllQuestions() {
        return questionRepository.findAllByOrderByCreatedAtDesc().stream().map(q -> {
            QuestionWithAnswersDTO dto = new QuestionWithAnswersDTO();
            dto.setId(q.getId());
            dto.setTitle(q.getTitle());
            dto.setBody(q.getBody());

            dto.setAnswers(q.getAnswers().stream().map(a -> {
                AnswerDTO adto = toAnswerDTO(a, false);
                adto.setComments(List.of());
                return adto;
            }).toList());

            return dto;
        }).toList();
    }

    public Optional<Question> getQuestionById(String id) {
        return questionRepository.findById(id);
    }

    // Cache individual question summary by question id
    @Cacheable(value = "questionResponsesById", key = "#id")
    public QuestionResponse getQuestionResponseById(String id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        return new QuestionResponse(
                q.getId(),
                q.getTitle(),
                q.getBody(),
                voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
                q.getAnswers().size(),
                q.getCreatedAt(),
                q.getTags()
                        .stream()
                        .map(t -> new TagDTO(t.getId(), t.getName()))
                        .toList()
        );
    }

    // Cache full question detail by question id
    @Cacheable(value = "questionDetails", key = "#id")
    @Transactional(readOnly = true)
    public QuestionDetailResponse getQuestionDetail(String id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        List<AnswerDTO> answers = q.getAnswers().stream().map(a -> {
            AnswerDTO dto = toAnswerDTO(a, true);
            return dto;
        }).toList();

        return new QuestionDetailResponse(
                q.getId(),
                q.getTitle(),
                q.getBody(),
                voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
                q.getCreatedAt(),
                q.getTags().stream().map(Tag::getName).toList(),
                commentRepository.findByQuestion_IdOrderByCreatedAtAsc(q.getId())
                        .stream()
                        .map(this::toCommentDTO)
                        .toList(),
                answers
        );
    }

    // No cache here: list/feed can change often
    public List<QuestionResponse> getAllQuestionResponses() {
        return questionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getTitle(),
                        q.getBody(),
                        voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION),
                        q.getAnswers().size(),
                        q.getCreatedAt(),
                        q.getTags()
                                .stream()
                                .map(t -> new TagDTO(t.getId(), t.getName()))
                                .toList()
                ))
                .toList();
    }

    // New question has no questionDetails cache yet, so no cache eviction needed
    public Question createQuestion(QuestionRequest request) {
        Question q = new Question();
        q.setTitle(request.getTitle());
        q.setBody(request.getBody());

        Set<Tag> tagSet = new HashSet<>();

        List<String> tagIds = request.getTagIds() == null ? List.of() : request.getTagIds();
        for (String tagId : tagIds) {
            if (tagId == null) {
                continue;
            }

            tagSet.add(tagRepository.findById(tagId)
                    .orElseThrow(() -> new RuntimeException("Tag not found")));
        }

        List<String> tagNames = request.getTagNames() == null ? List.of() : request.getTagNames();
        for (String tagName : tagNames) {
            String normalizedTagName = normalizeTagName(tagName);
            if (normalizedTagName.isBlank()) {
                continue;
            }

            Tag tag = tagRepository.findByName(normalizedTagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(normalizedTagName);
                        return tagRepository.save(newTag);
                    });
            tagSet.add(tag);
        }

        q.setTags(tagSet);

        Question saved = questionRepository.save(q);
        kafkaEventProducer.publish(QuestionEvent.questionCreated(saved.getId(), null));

        return saved;
    }

    private String normalizeTagName(String tagName) {
        if (tagName == null) {
            return "";
        }

        return tagName.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9+#.-]", "");
    }

    private AnswerDTO toAnswerDTO(Answer answer, boolean includeComments) {
        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setBody(answer.getBody());
        dto.setUsername(answer.getUser() != null ? answer.getUser().getUsername() : "Unknown");
        dto.setVoteCount(voteRepository.getVoteCount(answer.getId(), Vote.EntityType.ANSWER));
        dto.setAccepted(answer.isAccepted());
        dto.setCreatedAt(answer.getCreatedAt());
        if (includeComments) {
            dto.setComments(commentRepository.findByAnswer_IdOrderByCreatedAtAsc(answer.getId())
                    .stream()
                    .map(this::toCommentDTO)
                    .toList());
        } else {
            dto.setComments(List.of());
        }
        return dto;
    }

    private CommentDTO toCommentDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getBody(),
                comment.getUser() != null ? comment.getUser().getUsername() : "Unknown",
                comment.getCreatedAt()
        );
    }

    // Only evict cache for this specific question
    @CacheEvict(value = { "questionResponsesById", "questionDetails" }, key = "#id")
    public void deleteQuestion(String id) {
        questionRepository.deleteById(id);
        kafkaEventProducer.publish(QuestionEvent.questionDeleted(id));
    }
}
