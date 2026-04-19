# Sporty Messaging App

Backend service that simulates sports-betting event-outcome handling and bet settlement using **Kafka** and **RocketMQ**.

## Flow

```
HTTP POST                Kafka                       RocketMQ
event-outcome  ── ▶  event-outcomes  ── ▶  match  ── ▶  bet-settlements  ── ▶  update H2
```

1. `POST /api/v1/event-outcomes` publishes an `EventOutcome` to the Kafka topic `event-outcomes`.
2. A Kafka listener consumes the message and asks `SettlementService` to find pending bets in H2 by `eventId`.
3. For each matching bet, the service emits a `BetSettlement` (WON/LOST + payout) to the RocketMQ topic `bet-settlements`.
4. A RocketMQ listener consumes the settlement message and updates the bet's status in H2.

## Tech stack

- Java 22, Spring Boot 3.3
- spring-kafka, rocketmq-spring-boot-starter 2.3
- Spring Data JPA + H2 (in-memory)
- Maven

## Prerequisites

- JDK 22
- Docker + Docker Compose
- Maven 3.9+ (or use the `mvn` available on your PATH)

## Run

### 1. Start Kafka and RocketMQ

```bash
docker compose up -d
```

This starts:
- Kafka (KRaft mode, single broker) on `localhost:9092`
- RocketMQ name server on `localhost:9876`
- RocketMQ broker on `localhost:10911`

Wait ~15 seconds for the RocketMQ broker to register with the name server.

### 2. Start the application

```bash
mvn spring-boot:run
```

On startup the app:
- Creates the Kafka topic `event-outcomes`.
- Subscribes to the RocketMQ topic `bet-settlements`.
- Seeds 4 sample bets via `SampleBetSeeder` (3 for `eventId=1`, 1 for `eventId=2`).

### 3. Trigger the flow

Publish an event outcome where the winner is `42` for event `1`:

```bash
curl -i -X POST http://localhost:8080/api/v1/event-outcomes \
  -H 'Content-Type: application/json' \
  -d '{"eventId":1,"eventName":"Lakers vs Celtics","eventWinnerId":42}'
```

Expected response: `202 Accepted`.

Watch the application logs — you should see, in order:

```
Publishing event outcome to Kafka topic 'event-outcomes': ...
Received event outcome from Kafka: ...
Found 3 pending bets for eventId=1
Sending settlement to RocketMQ topic 'bet-settlements': ...
Received settlement from RocketMQ: ...
Bet 1 settled as WON (payout=50.00)
Bet 2 settled as LOST (payout=0)
Bet 3 settled as WON (payout=100.00)
```

### 4. Verify

```bash
curl http://localhost:8080/api/v1/bets
```

Bets for `eventId=1` are now `WON` (those with `eventWinnerId=42`) or `LOST` (mismatch). The bet for `eventId=2` stays `PENDING` because no outcome was published for it.

You can also browse the H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bets`, user `sa`, no password).

## Tests

```bash
mvn test
```

Runs:
- `EventOutcomeControllerTest` — `@WebMvcTest` slice that asserts the controller returns `202` and invokes the producer, plus rejects invalid payloads with `400`.
- `SettlementServiceTest` — Mockito unit test that asserts WON / LOST classification and payout arithmetic.

The tests do not require Kafka or RocketMQ to be running.

## Teardown

```bash
docker compose down
```

## Project layout

```
src/main/java/org/example/
├── SportyMessagingApplication.java
├── api/                      # REST controllers + DTOs
├── domain/                   # EventOutcome, Bet, BetStatus, BetSettlement
├── repository/               # Spring Data JPA repository
├── kafka/                    # producer, consumer, topic config
├── rocketmq/                 # producer, @RocketMQMessageListener consumer
├── service/                  # SettlementService (matching logic)
└── bootstrap/                # SampleBetSeeder
```

## Design notes

- **In-memory H2** with `ddl-auto=create-drop` — schema is created on startup and discarded on shutdown.
- **Settlement decision lives in `SettlementService`**, while persistence of the final status happens in the `BetSettlementConsumer`. This keeps the flow faithful to the assignment: the Kafka consumer matches bets and emits settlement messages; the RocketMQ consumer settles them.
- **JSON serialization** is used for both Kafka (`spring.json.value.default.type` pinned to `EventOutcome`) and RocketMQ (default Jackson via `MessageBuilder`).
- **Auto-create topic** is enabled on both brokers so neither `event-outcomes` nor `bet-settlements` needs to be pre-created.
- Known limitation: `rocketmq-spring-boot-starter:2.3.3` pulls in older transitive dependencies (Netty, Jackson). Production use would require an explicit `<dependencyManagement>` block to override them — out of scope here.