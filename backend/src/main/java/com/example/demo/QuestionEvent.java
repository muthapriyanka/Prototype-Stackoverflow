package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionEvent {
    private String eventType;
    private String questionId;
    private String userId;
}
