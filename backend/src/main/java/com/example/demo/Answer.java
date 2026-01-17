package com.example.demo;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-answers")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference("question-answers")
    private Question question;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted;   // 

    public void setAccepted(boolean accepted) {
    this.isAccepted = accepted;
    }
    public boolean isAccepted() {
        return isAccepted;
    }
}

