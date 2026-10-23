# Kafka, explained from the beginning

## The idea in one sentence

Kafka is a message board for programs: one part writes a message, and another part reads it when ready.

Imagine a restaurant. A customer gives an order to the counter. The counter writes the order on a ticket rail. The cook reads tickets one at a time. The customer does not need to stand in the kitchen waiting for the cook. In this app, the scheduler is the counter, Kafka is the ticket rail, and the listener is the cook.

## The classroom flow in this project

```mermaid
sequenceDiagram
    participant Timer as Spring timer
    participant DB as Database
    participant Producer as KafkaTemplate producer
    participant Topic as publishing.jobs topic
    participant Listener as @KafkaListener consumer
    participant Mock as Mock publisher
    Timer->>DB: Find due rows with SCHEDULED status
    Timer->>Producer: Send the post id
    Producer->>Topic: Store a JSON event
    Listener->>Topic: Read next event
    Topic-->>Listener: publicationId
    Listener->>DB: Load post; skip if already PUBLISHED
    Listener->>Mock: Pretend to publish content
    Mock-->>Listener: Mock remote id
    Listener->>DB: Save PUBLISHED status
```

1. The API saves a post with a platform, text, scheduled time, and `SCHEDULED` status.
2. Every few seconds `PublishingScheduler` asks the repository for due `SCHEDULED` rows.
3. The scheduler changes each row to `QUEUED` and `PublishingProducer` sends its id to Kafka.
4. The `publishing.jobs` topic stores the event until a consumer reads it.
5. `PublishingConsumer` loads the row, checks its status, calls the mock publisher, and saves `PUBLISHED`.

The Kafka message is like a ticket number: `{ "publicationId": 42 }`. The database remains the filing cabinet containing the full post text and status.

## Kafka words without the jargon

| Word | Plain meaning | In this project |
|---|---|---|
| Message / event | A small note sent between program parts | `{"publicationId":42}` |
| Producer | The part that writes a note | `PublishingProducer` |
| Consumer | The part that reads and acts on a note | `PublishingConsumer` |
| Topic | A named message board / ticket rail | `publishing.jobs` |
| Broker | The Kafka server that stores topics | Docker container named `kafka` |
| Consumer group | A team sharing the work of reading a topic | `social-publisher` |
| Offset | A group's bookmark showing its reading position | Managed by Kafka |
| Partition | One ordered lane within a topic | This demo uses one lane |
| Dead-letter topic (DLQ) | A separate place for messages that keep failing | `publishing.jobs.DLT` |

## Topics, partitions, and ordering

A **topic** is a named stream. Think of a topic as a mailbox for one kind of message. A project may have `orders`, `payments`, and `emails` topics. This project has `publishing.jobs` for work to publish.

A **partition** is a lane inside a topic. With one lane, events are read in the order they were written to that lane. With several lanes, Kafka can let several consumers work at once. Events in different lanes can be processed in a different order, so partitions are a speed tool with an ordering tradeoff. This classroom demo uses one partition to keep the picture simple.

Kafka uses the message **key** to decide which lane receives an event. Messages with the same key go to the same partition. This app uses the post id as the key. A one-partition topic means every key still goes to that one lane.

## Why use Kafka here?

Without Kafka, the timer would find a due row and call the publisher immediately. If the publisher were slow, the timer would wait. Kafka inserts a small waiting line: the timer writes a ticket and moves on; a consumer handles the ticket separately. Kafka also keeps messages while the consumer is temporarily stopped.

Use Kafka when separate parts need to exchange work asynchronously, when a queue of work is useful, or when several consumers need to share a stream.

Do not add Kafka to every small program. For a tiny app with one function calling another, a direct method call is easier to understand. Kafka adds a server, configuration, message formats, and new failure cases. Choose it when that extra separation is useful.

## Retry and dead-letter queue

If the mock/real publisher throws an error, the listener lets the error reach Spring Kafka. This demo tries again twice, waiting one second between attempts. If it still fails, Kafka sends the event to `publishing.jobs.DLT`. A DLQ is like a tray for tickets a cook could not complete; a person can inspect and replay them later. This demo demonstrates routing there, but does not include a DLQ dashboard or replay button.

The consumer checks the database before publishing. If the row is already `PUBLISHED`, it returns without calling the publisher again. This is a simple duplicate-event guard. It works for this lesson, though a real external API can succeed just before an app crash, so real systems need stronger ways to avoid duplicate posts.

## Run it

You need Docker Desktop, Java 17, and the included Maven wrapper. You do not install Kafka on your computer.

```powershell
docker compose up --build
```

Compose starts Kafka in KRaft mode, PostgreSQL, and the app. KRaft means Kafka keeps its own cluster notes; this setup does not need a separate ZooKeeper service.

The app has login protection. In another PowerShell window, log in with the local demo user, then schedule a post:

```powershell
$login = Invoke-RestMethod -Method Post http://localhost:8080/api/auth/login `
  -ContentType 'application/json' -Body '{"username":"user","password":"change-me-user"}'
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
$body = '{"platform":"X","content":"Hello from a Kafka classroom demo","scheduledAt":"2026-10-20T00:00:00Z"}'
$job = Invoke-RestMethod -Method Post http://localhost:8080/api/publishing/jobs `
  -Headers $headers -ContentType 'application/json' -Body $body
Start-Sleep -Seconds 5
Invoke-RestMethod "http://localhost:8080/api/publishing/jobs/$($job.id)" -Headers $headers
```

The returned status should become `PUBLISHED`; the remote id begins with `mock:`. Stop with `docker compose down`. Add `-v` only when you want to erase local database and Kafka data too.

## Find each part in the source

- `PublishingScheduler`: timer and database query.
- `PublishingProducer`: `KafkaTemplate.send(...)`.
- `PublishingConsumer`: `@KafkaListener`, duplicate check, publisher call, status update.
- `SocialMediaPublisher` and `MockSocialMediaPublisher`: simple publisher contract and local pretend implementation.
- `KafkaConfiguration`: JSON and retry/DLQ settings.
- `KafkaTopicConfiguration`: creates the two topic lanes (main and DLQ).

## Try these small experiments

1. Change the scheduler delay and notice how quickly an overdue post is picked up.
2. Add a `System.out.println` in the producer and consumer. Which one runs first?
3. Send the same publication id twice after it is published. Does the mock get called again?
4. Temporarily make the mock throw an exception. Watch the retries and then inspect `publishing.jobs.DLT` with `docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic publishing.jobs.DLT --from-beginning`.
5. Explain why the topic contains an id while the database contains the post text.

## Check your understanding

- Who is the producer? Who is the consumer?
- What is a topic? What is the topic name in this app?
- What does a partition help Kafka do? Why is one partition simpler to learn with?
- What does the consumer group's offset remember?
- What happens after repeated failures? What is the DLQ for?
- When would a direct method call be simpler than Kafka?
