package com.example.demo;

public class VoteResponse {

    private String entityId;
    private Vote.EntityType entityType;
    private Integer userVote;
    private long voteCount;

    public VoteResponse() {
    }

    public VoteResponse(String entityId, Vote.EntityType entityType, Integer userVote, long voteCount) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.userVote = userVote;
        this.voteCount = voteCount;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Vote.EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(Vote.EntityType entityType) {
        this.entityType = entityType;
    }

    public Integer getUserVote() {
        return userVote;
    }

    public void setUserVote(Integer userVote) {
        this.userVote = userVote;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }
}
