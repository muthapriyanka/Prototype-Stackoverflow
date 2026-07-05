package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.FeedItem;
import com.example.demo.FeedItemResponse;
import com.example.demo.Question;
import com.example.demo.TagDTO;
import com.example.demo.Vote;
import com.example.demo.repository.FeedItemRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.VoteRepository;

@Service
public class FeedService {

    private final FeedItemRepository feedItemRepository;
    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;

    public FeedService(FeedItemRepository feedItemRepository,
                       QuestionRepository questionRepository,
                       VoteRepository voteRepository) {
        this.feedItemRepository = feedItemRepository;
        this.questionRepository = questionRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public void addQuestionToFeed(String questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        FeedItem item = new FeedItem();
        item.setId(UUID.randomUUID().toString());
        item.setQuestionId(q.getId());
        item.setTitle(q.getTitle());
        item.setAnswerCount(q.getAnswers().size());
        item.setVoteCount(voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION));
        item.setCreatedAt(q.getCreatedAt());
        item.setLatestActivityAt(q.getCreatedAt());

        feedItemRepository.save(item);
        System.out.println("Inserted into feed_items: " + questionId);
    }

    @Transactional
    public void removeQuestionFromFeed(String questionId) {
        feedItemRepository.deleteByQuestionId(questionId);
    }

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getFeed() {
        return toResponses(feedItemRepository.findAllByOrderByLatestActivityAtDesc());
    }
    @Transactional
    public void refreshQuestionInFeed(String questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        FeedItem item = feedItemRepository.findByQuestionId(questionId)
                .orElseGet(() -> {
                    FeedItem newItem = new FeedItem();
                    newItem.setId(UUID.randomUUID().toString());
                    newItem.setQuestionId(q.getId());
                    newItem.setCreatedAt(q.getCreatedAt());
                    return newItem;
                });

        item.setTitle(q.getTitle());
        item.setAnswerCount(q.getAnswers().size());
        item.setVoteCount(voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION));
        item.setLatestActivityAt(LocalDateTime.now());

        feedItemRepository.save(item);
    }

    @Transactional
    public void refreshQuestionScore(String questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        FeedItem item = feedItemRepository.findByQuestionId(questionId)
                .orElseGet(() -> {
                    FeedItem newItem = new FeedItem();
                    newItem.setId(UUID.randomUUID().toString());
                    newItem.setQuestionId(q.getId());
                    newItem.setTitle(q.getTitle());
                    newItem.setAnswerCount(q.getAnswers().size());
                    newItem.setCreatedAt(q.getCreatedAt());
                    newItem.setLatestActivityAt(q.getCreatedAt());
                    return newItem;
                });

        item.setVoteCount(voteRepository.getVoteCount(q.getId(), Vote.EntityType.QUESTION));

        feedItemRepository.save(item);

        System.out.println("Updated feed_items vote_count for questionId = " + questionId);
    }

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getFeed(String sort) {
        List<FeedItem> items = switch (sort == null ? "active" : sort.toLowerCase()) {
            case "newest" -> feedItemRepository.findAllByOrderByCreatedAtDesc();
            case "score" -> feedItemRepository.findAllByOrderByVoteCountDesc();
            case "active" -> feedItemRepository.findAllByOrderByLatestActivityAtDesc();
            default -> feedItemRepository.findAllByOrderByLatestActivityAtDesc();
        };

        return toResponses(items);
    }

    private List<FeedItemResponse> toResponses(List<FeedItem> items) {
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    private FeedItemResponse toResponse(FeedItem item) {
        Question question = questionRepository.findById(item.getQuestionId()).orElse(null);

        String body = question != null ? question.getBody() : null;
        List<TagDTO> tags = question == null
                ? List.of()
                : question.getTags().stream()
                        .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                        .sorted(Comparator.comparing(TagDTO::getName))
                        .toList();

        return new FeedItemResponse(
                item.getId(),
                item.getQuestionId(),
                item.getTitle(),
                body,
                item.getAnswerCount(),
                item.getVoteCount(),
                item.getLatestActivityAt(),
                item.getCreatedAt(),
                tags
        );
    }
}
