# notification-service

Two things, both pushed live to the browser over Server-Sent Events:

- personal notifications for every role, each with its own read state
- the coordinator's activity feed

This service is part of **Assay**, a final-year-project grading platform. The architecture is described in
[fyp-grading-docker-compose/docs/architecture.md](https://github.com/FirasKhalife/fyp-grading-docker-compose/blob/master/docs/architecture.md).

## API

| Method and path | Who | What |
|---|---|---|
| `GET /api/notifications?unread=&page=&size=` | every role | My notifications, newest first, with `unreadCount` |
| `PATCH /api/notifications/{id}` | owner | Mark one read or unread |
| `POST /api/notifications/read-all` | every role | Mark all mine read |
| `GET /api/notifications/stream` | every role | SSE: `notification` and `unread-count` events, plus `activity` for admins |
| `GET /api/activity` | admin | The activity feed |

## Events and calls

- **Consumes** two exchanges:
  - `assay.notifications`, from admin-service, with the recipients already resolved.
  - `assay.activity`, from admin-service and evaluation-service.
- Both consumers are idempotent: an event key or id is stored once per copy and recipient.
- Failed messages are retried, then go to each queue's own dead-letter queue.
- Live events are pushed only after the row is committed.

## Rules it enforces

- A browser `EventSource` can't send headers, so the SPA opens the stream with a fetch-based client that sends the token and `X-Demo-Session`.

## Run it

The whole stack runs from [fyp-grading-docker-compose](https://github.com/FirasKhalife/fyp-grading-docker-compose).
To run this service on its own (Java 25, Maven):

```bash
./mvnw spring-boot:run
```

It needs Postgres (`notification-db`), RabbitMQ and Keycloak.

## Test

```bash
./mvnw test
```

`NotificationFlowTest` runs on real Postgres and RabbitMQ. It checks that:

- an event becomes one notification per recipient, even when it's redelivered
- read state is per person
- the feed is for coordinators only
- the seed gives each persona their own inbox Docker must be running for the Testcontainers tests.

## Notes for reviewers

- Spring Boot 4.0 and Spring Cloud 2025.1 on Java 25. The image is layered and runs as a non-root user.
- Every call needs a Keycloak token and an `X-Demo-Session` header.
- Each visitor gets a private copy of the data:
  - Every row is scoped to the visitor's session (Hibernate `@TenantId`).
  - A visitor's copy is created from `src/main/resources/demo/seed.json`.
  - Every copy is deleted at midnight (Europe/Paris).
- Errors are RFC 9457 problem details with a stable `code`.
- Roles are checked with method security. Identity always comes from the token.
- Actuator exposes only health, info and prometheus, and the gateway doesn't route it.
- The shared API contract lives in the workspace (`contract/openapi.yaml`). This service publishes its own part
  at `/v3/api-docs`.

## Lint and CI

- **Formatting:** Spotless with palantir-java-format. `./mvnw spotless:apply` fixes it.
- **Lint:** Checkstyle (`checkstyle.xml`) catches likely bugs and sloppy code.
- The build checks both in the `validate` phase, so `./mvnw verify` runs the lint, the build and every test.
- **GitHub Actions** (`.github/workflows/ci.yml`) runs on every pull request and on pushes to `master`:
  - the lint
  - `verify`, including the Testcontainers tests
  - a build of the Docker image
