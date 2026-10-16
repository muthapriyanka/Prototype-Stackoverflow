package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 

//JUnit is used to automatically test small pieces of Java code, 
// usually individual methods or classes. you do not write unit tests for every class and every method.
//You write unit tests for methods/classes where there is logic.

class QuestionEventTest {

    private String questionId;
    private String answerId;
    private String voteId;
    private String userId;

    //A setup method is a method that runs before each test to prepare common data.
    @BeforeEach
    void setUp() {
        questionId = "question-1";
        answerId = "answer-1";
        voteId = "vote-1";
        userId = "user-1";
    }

    @Test
    void questionCreatedBuildsQuestionCreatedEvent() {
        QuestionEvent event = QuestionEvent.questionCreated(questionId, userId);

        assertEquals("QUESTION_CREATED", event.getEventType());
        assertEquals(questionId, event.getEntityId());
        assertEquals(questionId, event.getQuestionId());
        assertEquals(userId, event.getUserId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void answerCreatedBuildsAnswerCreatedEvent() {
        QuestionEvent event = QuestionEvent.answerCreated(answerId, questionId, userId);

        assertEquals("ANSWER_CREATED", event.getEventType());
        assertEquals(answerId, event.getEntityId());
        assertEquals(questionId, event.getQuestionId());
        assertEquals(userId, event.getUserId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void voteUpdatedBuildsVoteUpdatedEvent() {
        QuestionEvent event = QuestionEvent.voteUpdated(voteId, questionId, userId);

        assertEquals("VOTE_UPDATED", event.getEventType());
        assertEquals(voteId, event.getEntityId());
        assertEquals(questionId, event.getQuestionId());
        assertEquals(userId, event.getUserId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void questionDeletedBuildsQuestionDeletedEventWithoutUser() {
        QuestionEvent event = QuestionEvent.questionDeleted(questionId);

        assertEquals("QUESTION_DELETED", event.getEventType());
        assertEquals(questionId, event.getEntityId());
        assertEquals(questionId, event.getQuestionId());
        assertNull(event.getUserId());
        assertNotNull(event.getOccurredAt());
    }
}
