START TRANSACTION;

DELETE FROM votes;
DELETE FROM question_tags;
DELETE FROM feed_items;
DELETE FROM answers;
DELETE FROM questions;
DELETE FROM tags;

INSERT INTO users (id, created_at, email, password, username) VALUES
('50000000-0000-0000-0000-000000000001', '2026-07-05 08:00:00.000000', 'maya.backend@example.com', 'password', 'maya_backend'),
('50000000-0000-0000-0000-000000000002', '2026-07-05 08:05:00.000000', 'noah.frontend@example.com', 'password', 'noah_frontend'),
('50000000-0000-0000-0000-000000000003', '2026-07-05 08:10:00.000000', 'ira.search@example.com', 'password', 'ira_search'),
('50000000-0000-0000-0000-000000000004', '2026-07-05 08:15:00.000000', 'sam.ops@example.com', 'password', 'sam_ops')
ON DUPLICATE KEY UPDATE
email = VALUES(email),
password = VALUES(password),
username = VALUES(username);

SET @maya = (SELECT id FROM users WHERE username = 'maya_backend' LIMIT 1);
SET @noah = (SELECT id FROM users WHERE username = 'noah_frontend' LIMIT 1);
SET @ira = (SELECT id FROM users WHERE username = 'ira_search' LIMIT 1);
SET @sam = (SELECT id FROM users WHERE username = 'sam_ops' LIMIT 1);

INSERT INTO tags (id, created_at, name) VALUES
('30000000-0000-0000-0000-000000000001', '2026-07-05 08:20:00.000000', 'kafka'),
('30000000-0000-0000-0000-000000000002', '2026-07-05 08:21:00.000000', 'redis'),
('30000000-0000-0000-0000-000000000003', '2026-07-05 08:22:00.000000', 'java'),
('30000000-0000-0000-0000-000000000004', '2026-07-05 08:23:00.000000', 'multithreading'),
('30000000-0000-0000-0000-000000000005', '2026-07-05 08:24:00.000000', 'spring-boot'),
('30000000-0000-0000-0000-000000000006', '2026-07-05 08:25:00.000000', 'caching'),
('30000000-0000-0000-0000-000000000007', '2026-07-05 08:26:00.000000', 'docker'),
('30000000-0000-0000-0000-000000000008', '2026-07-05 08:27:00.000000', 'mysql'),
('30000000-0000-0000-0000-000000000009', '2026-07-05 08:28:00.000000', 'elasticsearch'),
('30000000-0000-0000-0000-000000000010', '2026-07-05 08:29:00.000000', 'system-design'),
('30000000-0000-0000-0000-000000000011', '2026-07-05 08:30:00.000000', 'backend'),
('30000000-0000-0000-0000-000000000012', '2026-07-05 08:31:00.000000', 'database');

INSERT INTO questions (id, created_at, title, body, user_id) VALUES
('10000000-0000-0000-0000-000000000001', '2026-07-05 09:00:00.000000', 'What is Kafka and why do backend systems use it?', 'I keep hearing Kafka in system design discussions. Is Kafka just a queue, or is it different from a normal message broker?', @maya),
('10000000-0000-0000-0000-000000000002', '2026-07-05 09:15:00.000000', 'What is Redis used for in a web application?', 'I know Redis stores key-value data in memory, but I am confused about when to use it for caching, sessions, rate limits, or queues.', @noah),
('10000000-0000-0000-0000-000000000003', '2026-07-05 09:30:00.000000', 'How do I work with Redis cache in Spring Boot?', 'I want to cache the list of questions in my Spring Boot API. What annotations should I use, and when should I evict the cache?', @sam),
('10000000-0000-0000-0000-000000000004', '2026-07-05 09:45:00.000000', 'What is multithreading in Java?', 'I understand that Java can run multiple threads, but what does that mean in real backend code? When does multithreading help?', @ira),
('10000000-0000-0000-0000-000000000005', '2026-07-05 10:00:00.000000', 'What is the difference between a process and a thread?', 'Both process and thread sound like ways to run code at the same time. What is the practical difference between them?', @maya),
('10000000-0000-0000-0000-000000000006', '2026-07-05 10:15:00.000000', 'How do Kafka consumer groups work?', 'If two services read from the same Kafka topic, do they both get every message? How do partitions and consumer groups affect this?', @noah),
('10000000-0000-0000-0000-000000000007', '2026-07-05 10:30:00.000000', 'What is Elasticsearch and why use it for search?', 'I already store questions in MySQL. Why would I also send question data to Elasticsearch for searching?', @ira),
('10000000-0000-0000-0000-000000000008', '2026-07-05 10:45:00.000000', 'How can I run Kafka, Redis, MySQL, and Elasticsearch locally?', 'I want to learn production-style backend tools without paying for AWS. Is Docker Compose a good way to run these services locally?', @sam);

INSERT INTO answers (id, created_at, body, is_accepted, question_id, user_id) VALUES
('20000000-0000-0000-0000-000000000001', '2026-07-05 09:05:00.000000', 'Kafka is an event streaming platform. Producers write events to topics, and consumers read those events later. It is useful when multiple parts of a system need to react to the same change.', b'1', '10000000-0000-0000-0000-000000000001', @ira),
('20000000-0000-0000-0000-000000000002', '2026-07-05 09:08:00.000000', 'A queue usually sends one message to one worker. Kafka keeps events in a topic for a retention period, so multiple consumer groups can read the same event independently.', b'0', '10000000-0000-0000-0000-000000000001', @sam),
('20000000-0000-0000-0000-000000000003', '2026-07-05 09:20:00.000000', 'Redis is most commonly used as a fast cache. You can store data that is expensive to fetch from MySQL, such as popular question lists or user session data.', b'1', '10000000-0000-0000-0000-000000000002', @maya),
('20000000-0000-0000-0000-000000000004', '2026-07-05 09:23:00.000000', 'Because Redis is in memory, reads are very fast. The tradeoff is that you should treat cached data as temporary and keep the source of truth in a durable database.', b'0', '10000000-0000-0000-0000-000000000002', @ira),
('20000000-0000-0000-0000-000000000005', '2026-07-05 09:35:00.000000', 'In Spring Boot, use @Cacheable on read methods and @CacheEvict on writes that make cached data stale. For example, creating a question should evict the cached question list.', b'1', '10000000-0000-0000-0000-000000000003', @maya),
('20000000-0000-0000-0000-000000000006', '2026-07-05 09:39:00.000000', 'Use clear cache names like questions or questionDetails. Keep the cache key stable, and always think about which write operation should invalidate that data.', b'0', '10000000-0000-0000-0000-000000000003', @noah),
('20000000-0000-0000-0000-000000000007', '2026-07-05 09:51:00.000000', 'Multithreading means one process can run multiple execution paths at the same time. In backend code it helps with tasks like handling many requests, background work, or parallel processing.', b'1', '10000000-0000-0000-0000-000000000004', @sam),
('20000000-0000-0000-0000-000000000008', '2026-07-05 09:55:00.000000', 'It also introduces problems like race conditions and deadlocks. Shared mutable data should be protected with thread-safe structures or synchronization.', b'0', '10000000-0000-0000-0000-000000000004', @maya),
('20000000-0000-0000-0000-000000000009', '2026-07-05 10:05:00.000000', 'A process has its own memory space. A thread lives inside a process and shares that process memory with other threads. Threads are lighter, but shared memory needs care.', b'1', '10000000-0000-0000-0000-000000000005', @ira),
('20000000-0000-0000-0000-000000000010', '2026-07-05 10:22:00.000000', 'Consumers in the same group split partitions between themselves. Consumers in different groups each get their own copy of the topic stream.', b'1', '10000000-0000-0000-0000-000000000006', @maya),
('20000000-0000-0000-0000-000000000011', '2026-07-05 10:35:00.000000', 'MySQL is great for durable relational data. Elasticsearch is built for full-text search, ranking, token matching, and search filters, so it can make question search feel much better.', b'1', '10000000-0000-0000-0000-000000000007', @noah),
('20000000-0000-0000-0000-000000000012', '2026-07-05 10:50:00.000000', 'Docker Compose is a good local setup. You can run each service in a container, expose predictable ports, and keep your Spring Boot app configured through environment variables.', b'1', '10000000-0000-0000-0000-000000000008', @maya);

INSERT INTO question_tags (question_id, tag_id) VALUES
('10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000010'),
('10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000011'),
('10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000006'),
('10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000011'),
('10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000005'),
('10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006'),
('10000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000011'),
('10000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000003'),
('10000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000004'),
('10000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000010'),
('10000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000010'),
('10000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000011'),
('10000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000009'),
('10000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000010'),
('10000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000012'),
('10000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000001'),
('10000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000002'),
('10000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000007'),
('10000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000008'),
('10000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000009');

INSERT INTO votes (id, created_at, entity_id, entity_type, vote_type, user_id) VALUES
('40000000-0000-0000-0000-000000000001', '2026-07-05 09:06:00.000000', '10000000-0000-0000-0000-000000000001', 'QUESTION', 'UP', @noah),
('40000000-0000-0000-0000-000000000002', '2026-07-05 09:07:00.000000', '10000000-0000-0000-0000-000000000001', 'QUESTION', 'UP', @ira),
('40000000-0000-0000-0000-000000000003', '2026-07-05 09:08:00.000000', '10000000-0000-0000-0000-000000000001', 'QUESTION', 'UP', @sam),
('40000000-0000-0000-0000-000000000004', '2026-07-05 09:21:00.000000', '10000000-0000-0000-0000-000000000002', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000005', '2026-07-05 09:22:00.000000', '10000000-0000-0000-0000-000000000002', 'QUESTION', 'UP', @ira),
('40000000-0000-0000-0000-000000000006', '2026-07-05 09:36:00.000000', '10000000-0000-0000-0000-000000000003', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000007', '2026-07-05 09:37:00.000000', '10000000-0000-0000-0000-000000000003', 'QUESTION', 'UP', @noah),
('40000000-0000-0000-0000-000000000008', '2026-07-05 09:52:00.000000', '10000000-0000-0000-0000-000000000004', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000009', '2026-07-05 09:53:00.000000', '10000000-0000-0000-0000-000000000004', 'QUESTION', 'UP', @noah),
('40000000-0000-0000-0000-000000000010', '2026-07-05 09:54:00.000000', '10000000-0000-0000-0000-000000000004', 'QUESTION', 'UP', @sam),
('40000000-0000-0000-0000-000000000011', '2026-07-05 10:06:00.000000', '10000000-0000-0000-0000-000000000005', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000012', '2026-07-05 10:23:00.000000', '10000000-0000-0000-0000-000000000006', 'QUESTION', 'UP', @ira),
('40000000-0000-0000-0000-000000000013', '2026-07-05 10:24:00.000000', '10000000-0000-0000-0000-000000000006', 'QUESTION', 'UP', @sam),
('40000000-0000-0000-0000-000000000014', '2026-07-05 10:36:00.000000', '10000000-0000-0000-0000-000000000007', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000015', '2026-07-05 10:37:00.000000', '10000000-0000-0000-0000-000000000007', 'QUESTION', 'UP', @sam),
('40000000-0000-0000-0000-000000000016', '2026-07-05 10:51:00.000000', '10000000-0000-0000-0000-000000000008', 'QUESTION', 'UP', @maya),
('40000000-0000-0000-0000-000000000017', '2026-07-05 10:52:00.000000', '10000000-0000-0000-0000-000000000008', 'QUESTION', 'UP', @noah),
('40000000-0000-0000-0000-000000000018', '2026-07-05 10:53:00.000000', '10000000-0000-0000-0000-000000000008', 'QUESTION', 'UP', @ira);

INSERT INTO feed_items (id, question_id, title, answer_count, vote_count, latest_activity_at, created_at) VALUES
('60000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'What is Kafka and why do backend systems use it?', 2, 3, '2026-07-05 09:08:00', '2026-07-05 09:00:00'),
('60000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'What is Redis used for in a web application?', 2, 2, '2026-07-05 09:23:00', '2026-07-05 09:15:00'),
('60000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'How do I work with Redis cache in Spring Boot?', 2, 2, '2026-07-05 09:39:00', '2026-07-05 09:30:00'),
('60000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'What is multithreading in Java?', 2, 3, '2026-07-05 09:55:00', '2026-07-05 09:45:00'),
('60000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'What is the difference between a process and a thread?', 1, 1, '2026-07-05 10:05:00', '2026-07-05 10:00:00'),
('60000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000006', 'How do Kafka consumer groups work?', 1, 2, '2026-07-05 10:22:00', '2026-07-05 10:15:00'),
('60000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007', 'What is Elasticsearch and why use it for search?', 1, 2, '2026-07-05 10:35:00', '2026-07-05 10:30:00'),
('60000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008', 'How can I run Kafka, Redis, MySQL, and Elasticsearch locally?', 1, 3, '2026-07-05 10:50:00', '2026-07-05 10:45:00');

COMMIT;
