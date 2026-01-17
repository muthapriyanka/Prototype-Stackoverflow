// QuestionDetailResponse.java
package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class QuestionDetailResponse {

    private String id;
    private String title;
    private String body;
    private long voteCount;
    private LocalDateTime createdAt;
    private List<String> tags;
    private List<AnswerDTO> answers;

    public QuestionDetailResponse(
        String id,
        String title,
        String body,
        long voteCount,
        LocalDateTime createdAt,
        List<String> tags,
        List<AnswerDTO> answers
    ) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.voteCount = voteCount;
        this.createdAt = createdAt;
        this.tags = tags;
        this.answers = answers;
    }

    // getters
}
