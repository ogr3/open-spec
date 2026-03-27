## 1. Service Skeleton

- [x] 1.1 Scaffold a Spring Boot 4 (Java 25) Maven module (`username-service`) with starter parent, gitignore, and wrapper scripts.
- [x] 1.2 Configure baseline `application.yml`, add dependencies (spring-boot-starter-web, validation, data-jdbc, flyway, jackson, test) and verify `mvn verify` passes.

## 2. Handle Generation Domain

- [x] 2.1 Implement `HandleGenerationService` that emits trigram candidates following dot-preference, letter-only filtering, and padding semantics.
- [x] 2.2 Implement `ProfanityFilter` + `BlocklistLoader` that reads `blocklist-sv.json`, uppercases entries, and exposes lookup APIs.
- [x] 2.3 Add exhaustive JUnit/AssertJ tests covering Swedish characters, padding, blocklist fallbacks, and numeric suffix generation.

## 3. Persistence & Blocklist Infrastructure

- [x] 3.1 Create Flyway migration for `handles(handle VARCHAR PRIMARY KEY, email TEXT, created_at TIMESTAMP)` and configure PostgreSQL datasource profiles (local uses Testcontainers or docker-compose).
- [x] 3.2 Implement Spring Data repository/reservation service with optimistic retry + transactional guarantees.
- [x] 3.3 Write integration tests using Testcontainers PostgreSQL verifying reservation + release behavior and Flyway migration.

## 4. REST API Surface

- [x] 4.1 Build `POST /usernames` controller with request model validation and OpenAPI annotations.
- [x] 4.2 Wire controller to generator/reservation services and centralize error mapping in `@ControllerAdvice` producing `{ code, message, details }` responses.
- [x] 4.3 Add MockMvc/WebTestClient tests for happy path, invalid email (400), blocklist exhaustion (422), collision exhaustion (409), and storage outage (503).

## 5. Ops & Delivery

- [ ] 5.1 Add Dockerfile or Jib config, health endpoints, and Spring profiles for dev/staging/prod with env-driven DB + blocklist overrides.
- [ ] 5.2 Document blocklist update process and provide Maven exec/CLI script to deduplicate + append entries safely.
- [ ] 5.3 Wire CI workflow to run `mvn verify`, integration tests, and build/publish the container image.
