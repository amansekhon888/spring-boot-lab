# Event-driven architecture

An event says that something happened or should be handled. A queue separates the time the scheduler detects due work from the time a social publisher handles it. The application remains the owner of current job state in SQL; Kafka transports the work notification and provides buffering and consumer groups.

```mermaid
flowchart LR
  API[Schedule API] --> DB[(SQL job + outbox)]
  Timer[@Scheduled poller] --> DB
  DB --> Relay[Outbox relay]
  Relay --> Topic[(Kafka publishing.jobs)]
  Topic --> Worker[Kafka consumer]
  Worker --> Idem[(Processed event table)]
  Worker --> Adapter[Platform adapter]
  Topic -. exhausted attempts .-> DLT[(publishing.jobs.DLT)]
```

## The job state machine

`SCHEDULED` waits for its timestamp. A locked row becomes `QUEUED` with a lease and outbox event. The consumer sets `PUBLISHING`; success stores the remote id and `PUBLISHED`; exhausted processing becomes observable through the DLT and the database lease recovery handles abandoned claims. Retries use exponential backoff to avoid hammering a recovering provider. A production operator should distinguish retryable HTTP 429/5xx/timeouts from permanent 4xx errors, preserve `Retry-After`, and alert on repeated failures.

Kafka's idempotent producer avoids duplicates during a producer retry session. The SQL idempotency table handles repeated event deliveries after the consumer's successful transaction. Neither makes an arbitrary social HTTP API exactly-once: provider acceptance and the local commit are separate systems. Use provider-supported request keys and reconciliation for ambiguous network outcomes.

## Lab

1. Start Compose and observe Kafka's single-node KRaft controller/broker.
2. Schedule an item in the past; follow SQL state, outbox, topic, and status transitions.
3. Stop the app between outbox send and sent mark; restart and observe why consumers need idempotency.
4. Force a publisher exception in a local test and inspect exponential retry topics and the DLT.
5. Scale application instances and verify the database row lock permits one claim per item.

KRaft stores Kafka metadata in Kafka itself rather than requiring ZooKeeper. Compose is a local single-broker lab and has no production durability or availability guarantees.
