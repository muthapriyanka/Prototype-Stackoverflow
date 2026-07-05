package com.example.demo;

import java.util.List;

public class SearchResult {

    private String questionId;
    private String title;
    private String body;
    private List<String> tags;
    private long voteCount;
    private int answerCount;
    private String createdAt;
    private String latestActivityAt;
    private double score;

    public SearchResult() {
    }

    public SearchResult(String questionId, String title, String body, List<String> tags,
                        long voteCount, int answerCount, String createdAt,
                        String latestActivityAt, double score) {
        this.questionId = questionId;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.voteCount = voteCount;
        this.answerCount = answerCount;
        this.createdAt = createdAt;
        this.latestActivityAt = latestActivityAt;
        this.score = score;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLatestActivityAt() {
        return latestActivityAt;
    }

    public void setLatestActivityAt(String latestActivityAt) {
        this.latestActivityAt = latestActivityAt;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
