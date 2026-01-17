package com.example.demo;

import lombok.Data;

@Data
public class LoginResponse {
    private String userId;
    private String username;
public LoginResponse() {
    }

    public LoginResponse(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
