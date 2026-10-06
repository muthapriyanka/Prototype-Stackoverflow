package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByQuestion_IdOrderByCreatedAtAsc(String questionId);

    List<Comment> findByAnswer_IdOrderByCreatedAtAsc(String answerId);
}
