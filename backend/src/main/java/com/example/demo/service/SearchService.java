package com.example.demo.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Answer;
import com.example.demo.Question;
import com.example.demo.SearchResult;
import com.example.demo.Tag;
import com.example.demo.Vote;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.VoteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String elasticsearchUrl;
    private final String indexName;

    private volatile boolean indexReady;

    public SearchService(
            QuestionRepository questionRepository,
            VoteRepository voteRepository,
            @Value("${app.search.enabled:true}") boolean enabled,
            @Value("${app.search.elasticsearch-url:http://localhost:9200}") String elasticsearchUrl,
            @Value("${app.search.index:stackoverflow_questions}") String indexName
    ) {
        this.questionRepository = questionRepository;
        this.voteRepository = voteRepository;
        this.objectMapper = new ObjectMapper();
        this.enabled = enabled;
        this.elasticsearchUrl = elasticsearchUrl.replaceAll("/+$", "");
        this.indexName = indexName;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void bootstrapIndex() {
        if (!enabled) {
            return;
        }

        try {
            int indexed = reindexAllQuestions();
            log.info("Elasticsearch bootstrap indexed {} question documents", indexed);
        } catch (Exception ex) {
            log.warn("Elasticsearch bootstrap skipped. Search will use database fallback until Elasticsearch is ready.", ex);
        }
    }

    @Transactional(readOnly = true)
    public int reindexAllQuestions() {
        if (!enabled) {
            return 0;
        }

        try {
            ensureIndex();
            int count = 0;
            for (Question question : questionRepository.findAll()) {
                if (indexQuestionDocument(question)) {
                    count++;
                }
            }
            return count;
        } catch (Exception ex) {
            log.warn("Elasticsearch reindex failed", ex);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public void indexQuestion(String questionId) {
        if (!enabled || questionId == null || questionId.isBlank()) {
            return;
        }

        try {
            ensureIndex();
            questionRepository.findById(questionId).ifPresent(question -> indexQuestionDocument(question));
        } catch (Exception ex) {
            log.warn("Elasticsearch index update failed for questionId={}", questionId, ex);
        }
    }

    public void deleteQuestion(String questionId) {
        if (!enabled || questionId == null || questionId.isBlank()) {
            return;
        }

        try {
            ensureIndex();
            HttpResponse<String> response = send("DELETE", "/" + indexName + "/_doc/" + encode(questionId), null);
            if (response.statusCode() >= 400 && response.statusCode() != 404) {
                log.warn("Elasticsearch delete returned status={} body={}", response.statusCode(), response.body());
            }
        } catch (Exception ex) {
            log.warn("Elasticsearch delete failed for questionId={}", questionId, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(String query, String sort) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        if (!enabled) {
            return fallbackSearch(query, sort);
        }

        try {
            ensureIndex();
            return searchElasticsearch(query, sort);
        } catch (Exception ex) {
            log.warn("Elasticsearch search failed. Falling back to database search for query={}", query, ex);
            return fallbackSearch(query, sort);
        }
    }

    private void ensureIndex() throws IOException, InterruptedException {
        if (indexReady) {
            return;
        }

        synchronized (this) {
            if (indexReady) {
                return;
            }

            HttpResponse<String> head = send("HEAD", "/" + indexName, null);
            if (head.statusCode() == 200) {
                indexReady = true;
                return;
            }

            if (head.statusCode() != 404) {
                throw new IOException("Elasticsearch index check failed with status " + head.statusCode());
            }

            Map<String, Object> body = Map.of(
                    "mappings", Map.of(
                            "properties", Map.of(
                                    "questionId", Map.of("type", "keyword"),
                                    "title", Map.of("type", "text"),
                                    "body", Map.of("type", "text"),
                                    "answersText", Map.of("type", "text"),
                                    "tags", Map.of("type", "text", "fields", Map.of("keyword", Map.of("type", "keyword"))),
                                    "voteCount", Map.of("type", "integer"),
                                    "answerCount", Map.of("type", "integer"),
                                    "createdAt", Map.of("type", "date"),
                                    "latestActivityAt", Map.of("type", "date")
                            )
                    )
            );

            HttpResponse<String> put = send("PUT", "/" + indexName, objectMapper.writeValueAsString(body));
            if (put.statusCode() < 200 || put.statusCode() >= 300) {
                throw new IOException("Elasticsearch index create failed with status " + put.statusCode() + " body=" + put.body());
            }

            indexReady = true;
        }
    }

    private boolean indexQuestionDocument(Question question) {
        try {
            Map<String, Object> document = buildDocument(question);
            HttpResponse<String> response = send(
                    "PUT",
                    "/" + indexName + "/_doc/" + encode(question.getId()),
                    objectMapper.writeValueAsString(document)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Elasticsearch index returned status={} body={}", response.statusCode(), response.body());
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.warn("Elasticsearch document index failed for questionId={}", question.getId(), ex);
            return false;
        }
    }

    private Map<String, Object> buildDocument(Question question) {
        List<String> tags = question.getTags().stream()
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        String answersText = question.getAnswers().stream()
                .map(Answer::getBody)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        LocalDateTime createdAt = question.getCreatedAt();
        LocalDateTime latestActivityAt = question.getAnswers().stream()
                .map(Answer::getCreatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(createdAt);

        if (createdAt != null && latestActivityAt != null && createdAt.isAfter(latestActivityAt)) {
            latestActivityAt = createdAt;
        }

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("questionId", question.getId());
        document.put("title", question.getTitle());
        document.put("body", question.getBody());
        document.put("answersText", answersText);
        document.put("tags", tags);
        document.put("voteCount", voteRepository.getVoteCount(question.getId(), Vote.EntityType.QUESTION));
        document.put("answerCount", question.getAnswers().size());
        document.put("createdAt", toIsoString(createdAt));
        document.put("latestActivityAt", toIsoString(latestActivityAt));
        return document;
    }

    private List<SearchResult> searchElasticsearch(String query, String sort) throws IOException, InterruptedException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("size", 25);
        request.put("query", Map.of(
                "multi_match", Map.of(
                        "query", query,
                        "fields", List.of("title^4", "tags^3", "body^2", "answersText"),
                        "fuzziness", "AUTO"
                )
        ));

        List<Object> sortFields = elasticsearchSort(sort);
        if (!sortFields.isEmpty()) {
            request.put("sort", sortFields);
        }

        HttpResponse<String> response = send("POST", "/" + indexName + "/_search", objectMapper.writeValueAsString(request));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Elasticsearch search failed with status " + response.statusCode() + " body=" + response.body());
        }

        JsonNode hits = objectMapper.readTree(response.body()).path("hits").path("hits");
        List<SearchResult> results = new ArrayList<>();
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            results.add(new SearchResult(
                    source.path("questionId").asText(),
                    source.path("title").asText(),
                    excerpt(source.path("body").asText()),
                    readTags(source.path("tags")),
                    source.path("voteCount").asLong(0),
                    source.path("answerCount").asInt(0),
                    source.path("createdAt").asText(null),
                    source.path("latestActivityAt").asText(null),
                    hit.path("_score").asDouble(0.0)
            ));
        }

        return results;
    }

    private List<Object> elasticsearchSort(String sort) {
        String normalized = sort == null ? "relevance" : sort.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "newest" -> List.of(
                    Map.of("createdAt", Map.of("order", "desc")),
                    "_score"
            );
            case "score" -> List.of(
                    Map.of("voteCount", Map.of("order", "desc")),
                    "_score"
            );
            case "active" -> List.of(
                    Map.of("latestActivityAt", Map.of("order", "desc")),
                    "_score"
            );
            default -> List.of();
        };
    }

    private List<SearchResult> fallbackSearch(String query, String sort) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        List<SearchResult> results = questionRepository.findAll().stream()
                .map(question -> toSearchResult(question, relevanceScore(question, normalizedQuery)))
                .filter(result -> result.getScore() > 0)
                .collect(Collectors.toCollection(ArrayList::new));

        sortFallbackResults(results, sort);
        return results;
    }

    private SearchResult toSearchResult(Question question, double score) {
        Map<String, Object> document = buildDocument(question);
        return new SearchResult(
                question.getId(),
                question.getTitle(),
                excerpt(question.getBody()),
                (List<String>) document.get("tags"),
                ((Number) document.get("voteCount")).longValue(),
                ((Number) document.get("answerCount")).intValue(),
                (String) document.get("createdAt"),
                (String) document.get("latestActivityAt"),
                score
        );
    }

    private double relevanceScore(Question question, String normalizedQuery) {
        double score = 0;
        score += containsScore(question.getTitle(), normalizedQuery, 4);
        score += containsScore(question.getBody(), normalizedQuery, 2);
        score += question.getTags().stream()
                .map(Tag::getName)
                .map(tag -> containsScore(tag, normalizedQuery, 3))
                .reduce(0.0, Double::sum);
        score += question.getAnswers().stream()
                .map(Answer::getBody)
                .map(answer -> containsScore(answer, normalizedQuery, 1))
                .reduce(0.0, Double::sum);
        return score;
    }

    private double containsScore(String value, String normalizedQuery, double weight) {
        if (value == null) {
            return 0;
        }

        String normalizedValue = value.toLowerCase(Locale.ROOT);
        if (normalizedValue.contains(normalizedQuery)) {
            return weight;
        }

        double tokenScore = 0;
        for (String token : normalizedQuery.split("\\s+")) {
            if (!token.isBlank() && normalizedValue.contains(token)) {
                tokenScore += weight / 2;
            }
        }
        return tokenScore;
    }

    private void sortFallbackResults(List<SearchResult> results, String sort) {
        String normalized = sort == null ? "relevance" : sort.toLowerCase(Locale.ROOT);
        Comparator<SearchResult> comparator = switch (normalized) {
            case "newest" -> Comparator.comparing(SearchResult::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed();
            case "score" -> Comparator.comparingLong(SearchResult::getVoteCount).reversed()
                    .thenComparing(Comparator.comparingDouble(SearchResult::getScore).reversed());
            case "active" -> Comparator.comparing(SearchResult::getLatestActivityAt, Comparator.nullsLast(String::compareTo)).reversed();
            default -> Comparator.comparingDouble(SearchResult::getScore).reversed();
        };
        results.sort(comparator);
    }

    private HttpResponse<String> send(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(elasticsearchUrl + path))
                .timeout(Duration.ofSeconds(5));

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String toIsoString(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private List<String> readTags(JsonNode tagsNode) {
        List<String> tags = new ArrayList<>();
        if (tagsNode.isArray()) {
            tagsNode.forEach(node -> tags.add(node.asText()));
        }
        return tags;
    }

    private String excerpt(String value) {
        if (value == null) {
            return "";
        }

        String compact = value.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 220) {
            return compact;
        }
        return compact.substring(0, 217) + "...";
    }
}
