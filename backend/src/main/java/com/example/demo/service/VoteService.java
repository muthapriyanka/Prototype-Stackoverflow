package com.example.demo.service;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.QuestionEvent;
import com.example.demo.User;
import com.example.demo.Vote;
import com.example.demo.VoteRequest;
import com.example.demo.VoteResponse;
import com.example.demo.repository.VoteRepository;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final FeedService feedService;

    private final KafkaEventProducer kafkaEventProducer;

public VoteService(VoteRepository voteRepository, FeedService feedService, KafkaEventProducer kafkaEventProducer) {
    this.voteRepository = voteRepository;
    this.feedService = feedService;
    this.kafkaEventProducer = kafkaEventProducer;
}

    @Transactional
    @CacheEvict(value = { "questionResponsesById", "questionDetails" }, allEntries = true)
    public VoteResponse vote(VoteRequest request, User user) {
        if (request.getEntityId() == null || request.getEntityId().isBlank()) {
            throw new IllegalArgumentException("entityId is required");
        }
        if (request.getEntityType() == null) {
            throw new IllegalArgumentException("entityType is required");
        }
        if (request.getValue() != 1 && request.getValue() != -1) {
            throw new IllegalArgumentException("value must be 1 or -1");
        }

        Vote.VoteType requestedVoteType = request.getValue() == 1
                ? Vote.VoteType.UP
                : Vote.VoteType.DOWN;

        Optional<Vote> existingVote = voteRepository.findByUser_IdAndEntityIdAndEntityType(
                user.getId(),
                request.getEntityId(),
                request.getEntityType()
        );

        Integer userVote;
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == requestedVoteType) {
                voteRepository.delete(vote);
                userVote = null;
            } else {
                vote.setVoteType(requestedVoteType);
                voteRepository.save(vote);
                userVote = request.getValue();
            }
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setEntityId(request.getEntityId());
            vote.setEntityType(request.getEntityType());
            vote.setVoteType(requestedVoteType);
            voteRepository.save(vote);
            userVote = request.getValue();
        }

        long voteCount = voteRepository.getVoteCount(request.getEntityId(), request.getEntityType());

        if (request.getEntityType() == Vote.EntityType.QUESTION) {
        kafkaEventProducer.publish(
            QuestionEvent.voteUpdated(
                request.getEntityId(),
                request.getEntityId(),
                user.getId()
            )
    );
}

        return new VoteResponse(request.getEntityId(), request.getEntityType(), userVote, voteCount);
    }
}
