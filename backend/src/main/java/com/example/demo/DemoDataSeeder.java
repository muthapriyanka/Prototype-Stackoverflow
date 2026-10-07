package com.example.demo;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Vote.EntityType;
import com.example.demo.Vote.VoteType;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.FeedItemRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;

@Component
@ConditionalOnProperty(name = "app.demo.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final FeedItemRepository feedItemRepository;

    public DemoDataSeeder(
            UserRepository userRepository,
            TagRepository tagRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            VoteRepository voteRepository,
            CommentRepository commentRepository,
            FeedItemRepository feedItemRepository
    ) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
        this.feedItemRepository = feedItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, User> users = Map.of(
                "maya_backend", ensureUser("maya_backend", "maya.backend@example.com"),
                "noah_frontend", ensureUser("noah_frontend", "noah.frontend@example.com"),
                "ira_search", ensureUser("ira_search", "ira.search@example.com"),
                "sam_ops", ensureUser("sam_ops", "sam.ops@example.com")
        );

        List<Question> questions = QUESTIONS.stream()
                .map(seed -> ensureQuestion(seed, users))
                .toList();

        QUESTIONS.forEach(seed -> ensureVotes(seed.title(), EntityType.QUESTION, seed.votes(), users));
        questions.forEach(this::refreshFeedItem);

        log.info("Demo questions, answers, comments, and votes are ready");
    }

    private User ensureUser(String username, String email) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setEmail(email);
                    user.setPassword("password");
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword("password");
                    return userRepository.save(user);
                });
    }

    private Tag ensureTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    return tagRepository.save(tag);
                });
    }

    private Question ensureQuestion(QuestionSeed seed, Map<String, User> users) {
        Question question = findQuestionByTitle(seed.title());
        question.setTitle(seed.title());
        question.setBody(seed.body());
        question.setUser(users.get(seed.author()));
        question.setTags(seed.tags().stream().map(this::ensureTag).collect(java.util.stream.Collectors.toSet()));

        Question saved = questionRepository.save(question);
        List<Answer> answers = seed.answers().stream()
                .map(answerSeed -> ensureAnswer(saved, answerSeed, users))
                .toList();

        seed.questionComments().forEach(comment -> ensureQuestionComment(saved, comment, users));
        answers.forEach(answer -> {
            seed.answerComments().getOrDefault(answer.getBody(), List.of())
                    .forEach(comment -> ensureAnswerComment(answer, comment, users));
            seed.answerVotes().getOrDefault(answer.getBody(), List.of())
                    .forEach(vote -> ensureVote(answer.getId(), EntityType.ANSWER, vote, users));
        });

        return saved;
    }

    private Question findQuestionByTitle(String title) {
        return questionRepository.findAll().stream()
                .filter(question -> title.equals(question.getTitle()))
                .findFirst()
                .orElseGet(Question::new);
    }

    private Answer ensureAnswer(Question question, AnswerSeed seed, Map<String, User> users) {
        return answerRepository.findByQuestion_Id(question.getId()).stream()
                .filter(answer -> seed.body().equals(answer.getBody()))
                .findFirst()
                .map(answer -> {
                    answer.setUser(users.get(seed.author()));
                    answer.setAccepted(seed.accepted());
                    return answerRepository.save(answer);
                })
                .orElseGet(() -> {
                    Answer answer = new Answer();
                    answer.setBody(seed.body());
                    answer.setQuestion(question);
                    answer.setUser(users.get(seed.author()));
                    answer.setAccepted(seed.accepted());
                    return answerRepository.save(answer);
                });
    }

    private void ensureQuestionComment(Question question, CommentSeed seed, Map<String, User> users) {
        boolean exists = commentRepository.findByQuestion_IdOrderByCreatedAtAsc(question.getId()).stream()
                .anyMatch(comment -> seed.body().equals(comment.getBody()));
        if (exists) {
            return;
        }

        Comment comment = new Comment();
        comment.setBody(seed.body());
        comment.setUser(users.get(seed.author()));
        comment.setQuestion(question);
        commentRepository.save(comment);
    }

    private void ensureAnswerComment(Answer answer, CommentSeed seed, Map<String, User> users) {
        boolean exists = commentRepository.findByAnswer_IdOrderByCreatedAtAsc(answer.getId()).stream()
                .anyMatch(comment -> seed.body().equals(comment.getBody()));
        if (exists) {
            return;
        }

        Comment comment = new Comment();
        comment.setBody(seed.body());
        comment.setUser(users.get(seed.author()));
        comment.setAnswer(answer);
        commentRepository.save(comment);
    }

    private void ensureVotes(String questionTitle, EntityType entityType, List<VoteSeed> votes, Map<String, User> users) {
        String questionId = findQuestionByTitle(questionTitle).getId();
        votes.forEach(vote -> ensureVote(questionId, entityType, vote, users));
    }

    private void ensureVote(String entityId, EntityType entityType, VoteSeed seed, Map<String, User> users) {
        User user = users.get(seed.username());
        VoteType voteType = seed.value() > 0 ? VoteType.UP : VoteType.DOWN;

        voteRepository.findByUser_IdAndEntityIdAndEntityType(user.getId(), entityId, entityType)
                .ifPresentOrElse(vote -> {
                    vote.setVoteType(voteType);
                    voteRepository.save(vote);
                }, () -> {
                    Vote vote = new Vote();
                    vote.setUser(user);
                    vote.setEntityId(entityId);
                    vote.setEntityType(entityType);
                    vote.setVoteType(voteType);
                    voteRepository.save(vote);
                });
    }

    private void refreshFeedItem(Question question) {
        List<Answer> answers = answerRepository.findByQuestion_Id(question.getId());
        LocalDateTime latestActivityAt = answers.stream()
                .map(Answer::getCreatedAt)
                .filter(date -> date != null)
                .max(Comparator.naturalOrder())
                .orElseGet(() -> question.getCreatedAt() == null ? LocalDateTime.now() : question.getCreatedAt());

        FeedItem item = feedItemRepository.findByQuestionId(question.getId())
                .orElseGet(() -> {
                    FeedItem feedItem = new FeedItem();
                    feedItem.setId(UUID.randomUUID().toString());
                    feedItem.setQuestionId(question.getId());
                    feedItem.setCreatedAt(question.getCreatedAt() == null ? LocalDateTime.now() : question.getCreatedAt());
                    return feedItem;
                });

        item.setTitle(question.getTitle());
        item.setAnswerCount(answers.size());
        item.setVoteCount(voteRepository.getVoteCount(question.getId(), EntityType.QUESTION));
        item.setLatestActivityAt(latestActivityAt);
        feedItemRepository.save(item);
    }

    private record QuestionSeed(
            String title,
            String body,
            String author,
            Set<String> tags,
            List<AnswerSeed> answers,
            List<CommentSeed> questionComments,
            List<VoteSeed> votes,
            Map<String, List<CommentSeed>> answerComments,
            Map<String, List<VoteSeed>> answerVotes
    ) {
    }

    private record AnswerSeed(String body, String author, boolean accepted) {
    }

    private record CommentSeed(String body, String author) {
    }

    private record VoteSeed(String username, int value) {
    }

    private static final List<QuestionSeed> QUESTIONS = List.of(
            new QuestionSeed(
                    "How can I run Kafka, Redis, MySQL, and Elasticsearch locally?",
                    "I want to learn production-style backend tools without paying for AWS. Is Docker Compose a good way to run these services locally?",
                    "sam_ops",
                    Set.of("docker", "kafka", "redis", "mysql", "elasticsearch"),
                    List.of(
                            new AnswerSeed("Docker Compose is a good local setup. You can run each service in a container, expose predictable ports, and keep your Spring Boot app configured through environment variables.", "maya_backend", true),
                            new AnswerSeed("Use local containers for learning, then move the same concepts to managed services later. Keep passwords and hostnames in environment variables so deployment stays clean.", "ira_search", false)
                    ),
                    List.of(new CommentSeed("This is exactly the right use case for Compose while you are learning.", "maya_backend")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("noah_frontend", 1), new VoteSeed("ira_search", 1)),
                    Map.of(
                            "Docker Compose is a good local setup. You can run each service in a container, expose predictable ports, and keep your Spring Boot app configured through environment variables.",
                            List.of(new CommentSeed("Also add health checks so Spring waits for dependencies to be ready.", "sam_ops"))
                    ),
                    Map.of(
                            "Docker Compose is a good local setup. You can run each service in a container, expose predictable ports, and keep your Spring Boot app configured through environment variables.",
                            List.of(new VoteSeed("sam_ops", 1), new VoteSeed("noah_frontend", 1)),
                            "Use local containers for learning, then move the same concepts to managed services later. Keep passwords and hostnames in environment variables so deployment stays clean.",
                            List.of(new VoteSeed("maya_backend", 1))
                    )
            ),
            new QuestionSeed(
                    "What is Kafka and why do backend systems use it?",
                    "I keep hearing Kafka in system design discussions. Is Kafka just a queue, or is it different from a normal message broker?",
                    "maya_backend",
                    Set.of("kafka", "system-design", "backend"),
                    List.of(
                            new AnswerSeed("Kafka is an event streaming platform. Producers write events to topics, and consumers read those events later. It is useful when multiple parts of a system need to react to the same change.", "ira_search", true),
                            new AnswerSeed("A queue usually sends one message to one worker. Kafka keeps events in a topic for a retention period, so multiple consumer groups can read the same event independently.", "sam_ops", false)
                    ),
                    List.of(new CommentSeed("Think of Kafka as a durable event log, not just a task queue.", "ira_search")),
                    List.of(new VoteSeed("noah_frontend", 1), new VoteSeed("ira_search", 1), new VoteSeed("sam_ops", 1)),
                    Map.of(
                            "Kafka is an event streaming platform. Producers write events to topics, and consumers read those events later. It is useful when multiple parts of a system need to react to the same change.",
                            List.of(new CommentSeed("The consumer group part is what made this click for me.", "noah_frontend"))
                    ),
                    Map.of(
                            "Kafka is an event streaming platform. Producers write events to topics, and consumers read those events later. It is useful when multiple parts of a system need to react to the same change.",
                            List.of(new VoteSeed("maya_backend", 1), new VoteSeed("noah_frontend", 1)),
                            "A queue usually sends one message to one worker. Kafka keeps events in a topic for a retention period, so multiple consumer groups can read the same event independently.",
                            List.of(new VoteSeed("ira_search", 1))
                    )
            ),
            new QuestionSeed(
                    "How do Kafka consumer groups work?",
                    "If two services read from the same Kafka topic, do they both get every message? How do partitions and consumer groups affect this?",
                    "noah_frontend",
                    Set.of("kafka", "system-design", "backend"),
                    List.of(
                            new AnswerSeed("Consumers in the same group split partitions between themselves. Consumers in different groups each receive the topic events independently.", "maya_backend", true),
                            new AnswerSeed("Partitions give parallelism inside one group. If a topic has one partition, only one consumer in that same group can actively read it, but another group can still read the full stream.", "sam_ops", false)
                    ),
                    List.of(new CommentSeed("This is why feed indexing and search indexing can use different consumer groups.", "maya_backend")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("ira_search", 1)),
                    Map.of(),
                    Map.of(
                            "Consumers in the same group split partitions between themselves. Consumers in different groups each receive the topic events independently.",
                            List.of(new VoteSeed("noah_frontend", 1), new VoteSeed("ira_search", 1)),
                            "Partitions give parallelism inside one group. If a topic has one partition, only one consumer in that same group can actively read it, but another group can still read the full stream.",
                            List.of(new VoteSeed("maya_backend", 1))
                    )
            ),
            new QuestionSeed(
                    "What is Redis used for in a web application?",
                    "I know Redis stores key-value data in memory, but I am confused about when to use it for caching, sessions, rate limits, or queues.",
                    "noah_frontend",
                    Set.of("redis", "caching", "backend"),
                    List.of(
                            new AnswerSeed("Redis is most commonly used as a fast cache. You can store data that is expensive to fetch from MySQL, such as popular question details or session data.", "maya_backend", true),
                            new AnswerSeed("Because Redis is in memory, reads are very fast. The tradeoff is that cached data should be treated as temporary while MySQL remains the source of truth.", "ira_search", false)
                    ),
                    List.of(new CommentSeed("A cache hit should avoid repeating the database query.", "sam_ops")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("ira_search", 1)),
                    Map.of(),
                    Map.of(
                            "Redis is most commonly used as a fast cache. You can store data that is expensive to fetch from MySQL, such as popular question details or session data.",
                            List.of(new VoteSeed("noah_frontend", 1), new VoteSeed("sam_ops", 1))
                    )
            ),
            new QuestionSeed(
                    "How do I cache question details with Redis in Spring Boot?",
                    "For a question detail API, should I cache the whole response by question id? When should that cached value be invalidated?",
                    "sam_ops",
                    Set.of("redis", "spring-boot", "caching"),
                    List.of(
                            new AnswerSeed("Use @Cacheable on the read method with the question id as the key. Use @CacheEvict when an answer, comment, or vote changes the detail response.", "maya_backend", true),
                            new AnswerSeed("Cache the response DTO, not the JPA entity. DTOs are simpler to serialize and avoid lazy-loading surprises.", "noah_frontend", false)
                    ),
                    List.of(new CommentSeed("This also makes it easier to compare cache-hit and database timings.", "ira_search")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("noah_frontend", 1)),
                    Map.of(),
                    Map.of(
                            "Use @Cacheable on the read method with the question id as the key. Use @CacheEvict when an answer, comment, or vote changes the detail response.",
                            List.of(new VoteSeed("sam_ops", 1), new VoteSeed("ira_search", 1))
                    )
            ),
            new QuestionSeed(
                    "What is Elasticsearch and why use it for search?",
                    "I already store questions in MySQL. Why would I also send question data to Elasticsearch for searching?",
                    "ira_search",
                    Set.of("elasticsearch", "system-design", "database"),
                    List.of(
                            new AnswerSeed("MySQL is great for durable relational data. Elasticsearch is built for full-text search, ranking, token matching, and filters, so it can make question search feel much better.", "noah_frontend", true),
                            new AnswerSeed("In this app, Kafka can publish a question-created event and a separate search consumer can index that question into Elasticsearch without slowing down the main request.", "maya_backend", false)
                    ),
                    List.of(new CommentSeed("So MySQL remains source of truth and Elasticsearch is the search index.", "sam_ops")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("sam_ops", 1)),
                    Map.of(),
                    Map.of(
                            "MySQL is great for durable relational data. Elasticsearch is built for full-text search, ranking, token matching, and filters, so it can make question search feel much better.",
                            List.of(new VoteSeed("ira_search", 1), new VoteSeed("maya_backend", 1)),
                            "In this app, Kafka can publish a question-created event and a separate search consumer can index that question into Elasticsearch without slowing down the main request.",
                            List.of(new VoteSeed("sam_ops", 1))
                    )
            ),
            new QuestionSeed(
                    "What is multithreading in Java?",
                    "I understand that Java can run multiple threads, but what does that mean in real backend code? When does multithreading help?",
                    "ira_search",
                    Set.of("java", "multithreading", "backend"),
                    List.of(
                            new AnswerSeed("Multithreading means one process can run multiple execution paths at the same time. In backend code it helps with handling many requests, background work, or parallel processing.", "sam_ops", true),
                            new AnswerSeed("It also introduces problems like race conditions and deadlocks. Shared mutable data should be protected with thread-safe structures or synchronization.", "maya_backend", false)
                    ),
                    List.of(new CommentSeed("Start with request threads and executor services; those are common interview examples.", "noah_frontend")),
                    List.of(new VoteSeed("maya_backend", 1), new VoteSeed("noah_frontend", 1), new VoteSeed("sam_ops", 1)),
                    Map.of(),
                    Map.of(
                            "Multithreading means one process can run multiple execution paths at the same time. In backend code it helps with handling many requests, background work, or parallel processing.",
                            List.of(new VoteSeed("ira_search", 1), new VoteSeed("maya_backend", 1))
                    )
            ),
            new QuestionSeed(
                    "How should I explain Kafka, Redis, and Elasticsearch in an interview?",
                    "I built a Stack Overflow style prototype using Kafka, Redis, and Elasticsearch. What is a clear way to explain why each component exists?",
                    "maya_backend",
                    Set.of("kafka", "redis", "elasticsearch", "system-design"),
                    List.of(
                            new AnswerSeed("Explain the responsibility of each tool: MySQL stores durable data, Redis speeds repeated reads, Kafka decouples side effects, and Elasticsearch powers full-text search.", "ira_search", true),
                            new AnswerSeed("Use one request flow. For example: create question saves to MySQL, publishes an event to Kafka, updates feed/search asynchronously, and Redis caches question detail reads.", "sam_ops", false)
                    ),
                    List.of(new CommentSeed("This is a good interview framing because it connects design choices to user-visible behavior.", "noah_frontend")),
                    List.of(new VoteSeed("noah_frontend", 1), new VoteSeed("ira_search", 1), new VoteSeed("sam_ops", 1)),
                    Map.of(),
                    Map.of(
                            "Explain the responsibility of each tool: MySQL stores durable data, Redis speeds repeated reads, Kafka decouples side effects, and Elasticsearch powers full-text search.",
                            List.of(new VoteSeed("maya_backend", 1), new VoteSeed("noah_frontend", 1), new VoteSeed("sam_ops", 1))
                    )
            )
    );
}
