package com.example.demo;

import lombok.Data;

@Data
public class CreateAnswerRequest {
    private String body;
    private String questionId;
    public String getBody() {
        return body;
    }

    public String getQuestionId() {
        return questionId;
    }
}