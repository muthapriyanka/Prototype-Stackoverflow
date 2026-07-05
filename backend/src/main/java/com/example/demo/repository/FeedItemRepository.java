package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.FeedItem;

public interface FeedItemRepository extends JpaRepository<FeedItem, String> {
    Optional<FeedItem> findByQuestionId(String questionId);
    List<FeedItem> findAllByOrderByLatestActivityAtDesc();
    List<FeedItem> findAllByOrderByCreatedAtDesc();

    List<FeedItem> findAllByOrderByVoteCountDesc();
    void deleteByQuestionId(String questionId);
}
