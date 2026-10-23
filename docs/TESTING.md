# Run the checks

The project uses Spring Boot's test starter, which includes JUnit 5 and Mockito.

```powershell
./mvnw.cmd verify
```

`PublishingSchedulerTest` checks that a due scheduled post is marked `QUEUED` and its id is sent to the producer. The project also has tests for its existing application features.

For a hands-on Kafka check, run `docker compose up --build`, schedule a post as described in [ARCHITECTURE.md](ARCHITECTURE.md), and fetch the job a few seconds later. A `PUBLISHED` status demonstrates that the producer, broker, listener, mock, and database update worked together.

The goal here is a small classroom example. It does not add a separate Kafka test cluster or contract-testing tools.
