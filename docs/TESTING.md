# Testing the publishing flow

Run `./mvnw verify` (PowerShell: `./mvnw.cmd verify`). Spring Boot Test supplies JUnit 5, Mockito, and MockMvc. Pact JUnit 5 and Testcontainers JUnit/Kafka are available for the next exercises; a Testcontainers test requires a working Docker daemon and a Pact test needs a consumer/provider interaction contract.

The checked-in unit tests prove that polling requests a fixed-size batch with a lease and that an event already recorded in the idempotency table does not call a publisher again. The existing context and JWT tests also run. For meaningful production confidence, extend these with:

```java
// Arrange a due row and assert the repository receives a write-locked bounded query.
// Use two independent transactions against PostgreSQL Testcontainers to test claim races.
// Send duplicate PublishEvent values to Kafka Testcontainers and verify one publisher call.
// Fail a publisher repeatedly; assert retry topic attempts and eventual .DLT delivery.
// Use MockMvc with a JWT to check 201 validation and job status serialization.
// Add Pact consumer expectations and run provider verification as a separate CI job.
```

Mocks are fast and isolate decision logic. MockMvc tests request mapping, validation, and security. Testcontainers validates database locking and broker behavior against real services. Pact catches incompatible changes between independently deployed consumers and providers.
