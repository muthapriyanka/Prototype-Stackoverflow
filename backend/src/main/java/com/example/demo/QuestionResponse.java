package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionResponse {

    private String id;
    private String title;
    private String body;
    private long voteCount;
    private int answerCount;
    private LocalDateTime createdAt;
    private List<TagDTO> tags;

     public QuestionResponse(String id, String title, String body, long voteCount, int answerCount, LocalDateTime createdAt, List<TagDTO> tags) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.voteCount = voteCount;
        this.answerCount = answerCount;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }

    public int getAnswerCount() { return answerCount; }
    public void setAnswerCount(int answerCount) { this.answerCount = answerCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

}

