package com.example.demo;

public class AnswerDTO {
    private String id;
    private String body;
    private String username;

    // Constructors
    public AnswerDTO() {}

    public AnswerDTO(String id, String body, String username) {
        this.id = id;
        this.body = body;
        this.username = username;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
