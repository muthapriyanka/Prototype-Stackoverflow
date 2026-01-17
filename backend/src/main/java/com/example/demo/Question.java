package com.example.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
@JoinColumn(name = "user_id", nullable = true)
@JsonBackReference("user-questions")
private User user;

@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
@JsonManagedReference("question-answers")
private List<Answer> answers = new ArrayList<>();


//     @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
// @JoinTable(
//     name = "question_tags",
//     joinColumns = @JoinColumn(name = "question_id"),
//     inverseJoinColumns = @JoinColumn(name = "tag_id")
// )
// private List<Tag> tags = new ArrayList<>();  // NO JsonManagedReference

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "question_tags",
    joinColumns = @JoinColumn(name = "question_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<Tag> tags = new HashSet<>();

    @Transient
    private int voteCount;
}
