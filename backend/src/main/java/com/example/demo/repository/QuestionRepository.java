package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    // List<Question> findByUserId(String userId); // find all questions by a user
        List<Question> findAllByOrderByCreatedAtDesc();

}
