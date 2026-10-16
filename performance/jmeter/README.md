# JMeter Performance Tests

Use these plans to measure the performance claims for the Stack Overflow prototype.
Run them against the local backend when possible. Free hosted services such as Render
can add cold-start and network noise, so they are not ideal for resume numbers.

## Install JMeter

On macOS with Homebrew:

```bash
brew install jmeter
```

Check it:

```bash
jmeter --version
```

## Redis Cache Test

Plan:

```text
performance/jmeter/question-detail-cache-test.jmx
```

Endpoint tested:

```text
GET /questions/{questionId}/detail
```

This is the endpoint where Redis caching makes sense because repeated question-detail
reads can otherwise hit MySQL again and again.

Run local services and backend first:

```bash
docker compose up -d
cd backend
./mvnw spring-boot:run
```

Get a question id from:

```bash
curl -s http://localhost:8080/questions
```

Run the JMeter plan:

```bash
jmeter -n \
  -t performance/jmeter/question-detail-cache-test.jmx \
  -Jhost=localhost \
  -Jport=8080 \
  -JquestionId=<QUESTION_ID> \
  -Jthreads=20 \
  -Jloops=50 \
  -l performance/results/question-detail-cache.jtl \
  -e \
  -o performance/results/question-detail-cache-report
```

Important: JMeter requires the report output folder to not already exist or to be
empty. Use a new folder name for each run if needed.

What to record:

```text
Average response time
95th percentile response time
Throughput requests/second
Error percentage
```

For a fair Redis explanation:

1. Run once with cache disabled or cold cache as baseline.
2. Run again with Redis cache enabled and warmed.
3. Compare average and p95 latency.
4. Check Hibernate SQL logs: repeated cached calls should not repeat the same DB queries.

Local-only cache reset:

```bash
docker compose exec redis redis-cli FLUSHALL
```

## Kafka Question Creation Test

Plan:

```text
performance/jmeter/create-question-kafka-test.jmx
```

Endpoint tested:

```text
POST /questions
```

This creates unique questions and lets the backend publish question-created events.
Use Kafka UI to confirm events are appearing:

```text
http://localhost:8085
```

Run:

```bash
jmeter -n \
  -t performance/jmeter/create-question-kafka-test.jmx \
  -Jhost=localhost \
  -Jport=8080 \
  -Jthreads=10 \
  -Jloops=20 \
  -l performance/results/create-question-kafka.jtl \
  -e \
  -o performance/results/create-question-kafka-report
```

What to record:

```text
Average POST /questions latency
Throughput requests/second
Error percentage
Kafka topic event count / Kafka UI evidence
```

## Kafka Async vs Synchronous Comparison

The app supports two question-create processing modes:

```text
QUESTION_CREATE_PROCESSING_MODE=kafka
```

Saves the question, publishes a Kafka event, and returns the HTTP response.
Feed and Elasticsearch updates happen later in Kafka consumers.

```text
QUESTION_CREATE_PROCESSING_MODE=sync
```

Saves the question, updates the feed, indexes the question in Elasticsearch, and
then returns the HTTP response.

Use the same JMeter plan for both runs:

```bash
jmeter -n \
  -t performance/jmeter/create-question-kafka-test.jmx \
  -Jhost=localhost \
  -Jport=8080 \
  -Jthreads=5 \
  -Jloops=10 \
  -l performance/results/<RUN_NAME>.jtl \
  -e \
  -o performance/results/<RUN_NAME>-report
```

Run the sync baseline:

```bash
cd backend
QUESTION_CREATE_PROCESSING_MODE=sync ./mvnw spring-boot:run
```

Run the async Kafka version:

```bash
cd backend
QUESTION_CREATE_PROCESSING_MODE=kafka ./mvnw spring-boot:run
```

After the Kafka run, verify consumer lag is 0:

```bash
docker compose exec -T kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9094 \
  --describe \
  --group stackoverflow-feed-local

docker compose exec -T kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9094 \
  --describe \
  --group stackoverflow-search-indexer
```

This is the fair Kafka comparison because both modes do the same business work.
The difference is whether feed/search work happens inside the request or after the
request through Kafka consumers.

## Kafka Answer Notification Test

Plan:

```text
performance/jmeter/create-answer-kafka-test.jmx
```

Endpoint tested:

```text
POST /answers
```

The app supports two answer-create processing modes:

```text
ANSWER_CREATE_PROCESSING_MODE=sync
```

Saves the answer, refreshes the feed, updates Elasticsearch, sends the notification,
and then returns the HTTP response.

```text
ANSWER_CREATE_PROCESSING_MODE=kafka
```

Saves the answer, publishes an `ANSWER_CREATED` Kafka event, and returns the HTTP
response. Feed refresh, Elasticsearch update, and notification sending happen in
Kafka consumers.

Run with more requests:

```bash
jmeter -n \
  -t performance/jmeter/create-answer-kafka-test.jmx \
  -Jhost=localhost \
  -Jport=8080 \
  -JquestionId=<QUESTION_ID> \
  -Jtoken=<JWT_TOKEN> \
  -Jthreads=20 \
  -Jloops=50 \
  -JrampUp=10 \
  -l performance/results/<RUN_NAME>.jtl \
  -e \
  -o performance/results/<RUN_NAME>-report
```

This sends:

```text
20 threads * 50 loops = 1000 requests
```

After the Kafka run, verify consumer lag is 0:

```bash
docker compose exec -T kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9094 \
  --describe \
  --group stackoverflow-notifications-local
```

## Real Email Notification

Mock email is the default. To send real email, run the backend with SMTP settings:

```bash
APP_NOTIFICATIONS_EMAIL_ENABLED=true \
APP_NOTIFICATIONS_EMAIL_FROM=<FROM_EMAIL> \
SPRING_MAIL_HOST=smtp.gmail.com \
SPRING_MAIL_PORT=587 \
SPRING_MAIL_USERNAME=<SMTP_USERNAME> \
SPRING_MAIL_PASSWORD=<SMTP_APP_PASSWORD> \
SPRING_MAIL_SMTP_AUTH=true \
SPRING_MAIL_SMTP_STARTTLS_ENABLE=true \
ANSWER_CREATE_PROCESSING_MODE=kafka \
./mvnw spring-boot:run
```

Use a small request count for real email first, for example `threads=1` and
`loops=1`. Do not run the 1000-request JMeter test against a real inbox unless you
intentionally want to send 1000 emails and your SMTP provider allows it.

## Resume-Friendly Explanation

Use wording like this only after collecting numbers:

```text
Measured repeated question-detail reads using JMeter. After enabling Redis caching,
average latency improved by approximately X% and repeated Hibernate SQL queries were
reduced for cached responses. Kafka was used to decouple feed/search updates from the
main question creation flow, which I verified through Kafka UI and JMeter throughput
tests for POST /questions.
```

Avoid saying Kafka alone improved performance by a specific percentage unless you
measured a before/after baseline for synchronous versus asynchronous processing.
