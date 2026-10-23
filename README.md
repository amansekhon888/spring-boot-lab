# Spring Boot Social Media Scheduler

A small classroom project showing how a database scheduler sends a post id to Kafka and a listener handles it with a mock social publisher. Existing post CRUD and login endpoints are retained.

## Run the demonstration

Requirements: Docker Desktop and Java 17. Kafka runs in Docker Compose; do not install Kafka locally.

```powershell
docker compose up --build
```

Open [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the easiest explanation, a diagram, exact API commands, Kafka vocabulary, retries/DLQ, experiments, and questions. The local demo login is `user` / `change-me-user`.

Stop the services with `docker compose down`. Use `docker compose down -v` only to delete saved local database and Kafka data.

## Build and test

```powershell
./mvnw.cmd verify
```

The flow is: database → `@Scheduled` scheduler → `KafkaTemplate` producer → `publishing.jobs` topic → `@KafkaListener` consumer → mock publisher → database status update. See [docs/TESTING.md](docs/TESTING.md) for the small test and manual integration check.
