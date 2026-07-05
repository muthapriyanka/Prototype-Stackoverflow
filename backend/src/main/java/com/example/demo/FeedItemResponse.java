package com.example.demo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class FeedItemResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String questionId;
    private String title;
    private String body;
    private int answerCount;
    private int voteCount;
    private LocalDateTime latestActivityAt;
    private LocalDateTime createdAt;
    private List<TagDTO> tags;

    public FeedItemResponse() {
    }

    public FeedItemResponse(
            String id,
            String questionId,
            String title,
            String body,
            int answerCount,
            int voteCount,
            LocalDateTime latestActivityAt,
            LocalDateTime createdAt,
            List<TagDTO> tags
    ) {
        this.id = id;
        this.questionId = questionId;
        this.title = title;
        this.body = body;
        this.answerCount = answerCount;
        this.voteCount = voteCount;
        this.latestActivityAt = latestActivityAt;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public LocalDateTime getLatestActivityAt() {
        return latestActivityAt;
    }

    public void setLatestActivityAt(LocalDateTime latestActivityAt) {
        this.latestActivityAt = latestActivityAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }
}
