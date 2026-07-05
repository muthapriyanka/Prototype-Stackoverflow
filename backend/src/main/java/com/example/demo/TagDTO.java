package com.example.demo;

import java.io.Serializable;

public class TagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private long questionCount;

    public TagDTO() {
    }

    public TagDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagDTO(String id, String name, long questionCount) {
        this.id = id;
        this.name = name;
        this.questionCount = questionCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getQuestionCount() { return questionCount; }
    public void setQuestionCount(long questionCount) { this.questionCount = questionCount; }
}
