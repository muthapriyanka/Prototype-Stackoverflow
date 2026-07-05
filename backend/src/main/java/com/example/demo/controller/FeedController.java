package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.FeedItemResponse;
import com.example.demo.service.FeedService;


@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

   @GetMapping
    public List<FeedItemResponse> getFeed(@RequestParam(defaultValue = "active") String sort) {
        return feedService.getFeed(sort);
    }
}
