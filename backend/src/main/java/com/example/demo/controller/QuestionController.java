package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.AnswerDTO;
import com.example.demo.Question;
import com.example.demo.QuestionDetailResponse;
import com.example.demo.QuestionRequest;
import com.example.demo.QuestionResponse;
import com.example.demo.QuestionWithAnswersDTO;
import com.example.demo.service.QuestionService;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;


    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionResponse> getQuestions() {
        return questionService.getAllQuestionResponses();
    }
    
    @GetMapping("/with-answers")
public List<QuestionWithAnswersDTO> getQuestionsWithAnswers() {
    return questionService.getAllQuestions().stream().map(q -> {
        QuestionWithAnswersDTO dto = new QuestionWithAnswersDTO();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setBody(q.getBody());

        List<AnswerDTO> answers = q.getAnswers().stream().map(a -> {
            AnswerDTO adto = new AnswerDTO();
            adto.setId(a.getId());
            adto.setBody(a.getBody());

            adto.setUsername(a.getUsername() != null ? a.getUsername() : "Unknown");

            return adto;
        }).toList();


        dto.setAnswers(answers != null ? answers : new ArrayList<>());
        return dto;
    }).toList();
}


    @GetMapping("/{id}")
    public QuestionResponse getQuestion(@PathVariable String id) {
        return questionService.getQuestionResponseById(id);
    }

    @PostMapping
    public Question createQuestion(@RequestBody QuestionRequest request) {
    return questionService.createQuestion(request);
}

    @GetMapping("/{id}/detail")
    public QuestionDetailResponse getQuestionDetail(@PathVariable String id) {
        return questionService.getQuestionDetail(id);
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
    }
}
