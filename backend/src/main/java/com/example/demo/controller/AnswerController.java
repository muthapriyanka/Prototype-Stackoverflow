package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Answer;
import com.example.demo.CreateAnswerRequest;
import com.example.demo.User;
import com.example.demo.service.AnswerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/answers")
public class AnswerController {

    private final AnswerService answerService;
    

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @GetMapping("/question/{questionId}")
    public List<Answer> getAnswersByQuestion(@PathVariable String questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

@PostMapping
    public Answer createAnswer(@RequestBody CreateAnswerRequest request,
                               HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");

        if (user == null) {
            throw new RuntimeException("Unauthorized: user not authenticated");
        }

        return answerService.createAnswer(
            request.getBody(),
            request.getQuestionId(),
            user
        );
    }


    @DeleteMapping("/{id}")
    public void deleteAnswer(@PathVariable String id,
                             HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("Unauthorized");
        }

        answerService.deleteAnswer(id);
    }
}