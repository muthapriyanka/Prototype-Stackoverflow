package com.example.demo;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEvent {
    private String eventType;
    private String entityId;
    private String questionId;
    private String userId;
    private String occurredAt;

    public static QuestionEvent questionCreated(String questionId, String userId) {
        return new QuestionEvent("QUESTION_CREATED", questionId, questionId, userId, Instant.now().toString());
    }

    public static QuestionEvent questionDeleted(String questionId) {
        return new QuestionEvent("QUESTION_DELETED", questionId, questionId, null, Instant.now().toString());
    }

    public static QuestionEvent answerCreated(String answerId, String questionId, String userId) {
        return new QuestionEvent("ANSWER_CREATED", answerId, questionId, userId, Instant.now().toString());
    }

    public static QuestionEvent answerDeleted(String answerId, String questionId, String userId) {
        return new QuestionEvent("ANSWER_DELETED", answerId, questionId, userId, Instant.now().toString());
    }
    public static QuestionEvent voteCreated(String voteId, String questionId, String userId) {
    return new QuestionEvent("VOTE_CREATED", voteId, questionId, userId, Instant.now().toString());
    }

    public static QuestionEvent voteUpdated(String voteId, String questionId, String userId) {
        return new QuestionEvent("VOTE_UPDATED", voteId, questionId, userId, Instant.now().toString());
    }

    public static QuestionEvent voteDeleted(String voteId, String questionId, String userId) {
        return new QuestionEvent("VOTE_DELETED", voteId, questionId, userId, Instant.now().toString());
    }
}
