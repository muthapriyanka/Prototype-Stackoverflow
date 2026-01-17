package com.example.demo;

import java.util.List;

public class QuestionWithAnswersDTO {
    private String id;
    private String title;
    private String body;
    private List<AnswerDTO> answers;
    private List<Tag> tags;
public QuestionWithAnswersDTO() {}


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    public List<AnswerDTO> getAnswers() {
        return answers;
    }
    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }
}


