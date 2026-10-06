package com.example.demo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String body;
    private String username;
    private LocalDateTime createdAt;

    public CommentDTO() {
    }

    public CommentDTO(String id, String body, String username, LocalDateTime createdAt) {
        this.id = id;
        this.body = body;
        this.username = username;
        this.createdAt = createdAt;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
