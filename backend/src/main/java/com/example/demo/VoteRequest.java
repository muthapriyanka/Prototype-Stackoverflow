package com.example.demo;

import lombok.Data;

@Data
public class VoteRequest {
    private String entityId;
    private Vote.EntityType entityType;
    private int value; // +1 or -1
}
