package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.User;
import com.example.demo.VoteRequest;
import com.example.demo.VoteResponse;
import com.example.demo.service.VoteService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public VoteResponse vote(@RequestBody VoteRequest request, HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to vote");
        }

        return voteService.vote(request, user);
    }
}
