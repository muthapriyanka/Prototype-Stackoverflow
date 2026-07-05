package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.SearchResult;
import com.example.demo.service.SearchService;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<SearchResult> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "relevance") String sort
    ) {
        return searchService.search(query, sort);
    }

    @PostMapping("/reindex")
    public Map<String, Object> reindex() {
        int indexed = searchService.reindexAllQuestions();
        return Map.of("indexed", indexed);
    }
}
