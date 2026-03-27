## Context

We currently collect user email addresses during onboarding but lack an automated way to mint short usernames. Manual creation does not scale and risks offensive Swedish trigrams slipping through. The new service will expose a `POST /usernames` REST endpoint that encapsulates the derivation rules, profanity filtering, uniqueness checks, and persistence. The broader platform standardizes on Java microservices, so we will implement this service with Spring Boot 4 (Java 25) packaged with Maven, backed by PostgreSQL for reservations and a JSON blocklist file bundled with the application jar.

## Goals / Non-Goals

**Goals:**
- Provide a deterministic trigram derivation algorithm that honors the "character before dot + two after" preference yet gracefully degrades when inputs lack dots or letters.
- Guarantee that returned handles are unique and never match a Swedish profanity blocklist.
- Offer a single REST endpoint that validates inputs, reports errors clearly, and can be deployed independently.
- Make the blocklist and allocation store easy to seed in tests and local environments.

**Non-Goals:**
- Managing downstream profile creation; the service just returns/reserves handles.
- Supporting localization beyond Swedish profanity coverage.
- Building a UI for reviewing or editing reservations (CLI scripts will suffice initially).

## Decisions

1. **Service Skeleton** – Use Spring Boot 4 with Java 25 and Maven.
   - *Rationale:* Aligns with the rest of the stack, gives us first-class validation, observability, and production tooling. Spring Boot 4 starters reduce boilerplate while staying compatible with our deployment images.
   - *Alternatives:* Quarkus/Micronaut (leaner startup but unfamiliar to the team); Node-based service (diverges from org standards).

2. **Handle generation library** – Encapsulate derivation within a Spring `@Service` (`HandleGenerationService`) exposing a deterministic method `generateCandidates(email)`.
   - *Rationale:* Keeps business logic isolated from HTTP/persistence layers and makes it easy to unit test via JUnit + AssertJ.
   - *Details:* Method returns a `Stream<String>`/iterator that yields trigram windows followed by numeric suffix variants; controllers consume until success/failure.

3. **Profanity blocklist storage** – Ship a curated JSON file under `src/main/resources/blocklist-sv.json` and load it into a `Set<String>` bean at startup.
   - *Rationale:* The blocklist is tiny (~100 entries) and needs constant-time lookups. Bundling it in resources keeps deploys atomic while still allowing overrides via config volume if needed.
   - *Alternatives:* Database table (adds persistence coupling without benefit); remote API (adds latency and failure modes).

4. **Reservation store** – Use PostgreSQL with Spring Data JDBC (or JPA) and a single `handles(handle VARCHAR PRIMARY KEY, email TEXT, created_at TIMESTAMP)` table.
   - *Rationale:* PostgreSQL is our managed relational default, offers durable uniqueness guarantees, and scales horizontally via RDS. Spring Data reduces DAO boilerplate while still allowing explicit SQL when necessary.
   - *Alternatives:* SQLite (simpler but unsuitable for multi-instance deployments); Redis (fast but lacks strong durability guarantees we need).

5. **Validation & errors** – Use Jakarta Bean Validation plus Apache Commons Validator for email syntax; wrap failures in a global `@ControllerAdvice` that emits `{ code, message, details }` payloads.
   - *Rationale:* Keeps API predictable and testable while leaning on Spring's validation infrastructure. `details` will include cause codes like `invalid_email`, `all_blocked`, `collisions_exhausted`.

6. **Deployment** – Build a layered Docker image via Maven + Spring Boot plugin exposing port 8080.
   - *Rationale:* Aligns with existing Java services, allows reproducible builds, and keeps startup quick. PostgreSQL connection info supplied via environment variables.

## Risks / Trade-offs

- **PostgreSQL hotspotting** → Mitigate by adding a covering index on `handle`, using optimistic retries in the repository, and scaling read replicas if we later need lookup APIs.
- **Blocklist misses slang variants** → Allow ops to append entries and add telemetry on rejected handles to refine the list.
- **Generator exhaustion causing request latency** → Cap attempts (default 50) and log warnings when >10 iterations occur so we can inspect pathological addresses.
- **ASCII-only padding may conflict with names using Å/Ä/Ö** → Normalize Unicode to NFD and map Swedish letters to ASCII equivalents during window extraction; revisit if requirement changes.

## Migration Plan

1. Create Flyway migration V1__handles.sql that creates the `handles` table and apply it to dev/staging databases.
2. Check in `src/main/resources/blocklist-sv.json` plus sanity-check unit test that ensures no duplicates.
3. Deploy service to staging (Spring profile `staging`) pointing at the managed PostgreSQL instance; run load tests using synthetic email fixtures.
4. Enable production traffic by pointing onboarding to the new endpoint; keep manual fallback for 1 week.
5. Monitor logs/metrics for `all_blocked` or `collisions_exhausted` errors; adjust blocklist or suffix strategy if rates exceed 0.1%.
6. Document rollback: flip traffic back to manual handles (reservations remain in DB for future replay) and redeploy previous stable build.

## Open Questions

- Should we allow admins to manually reserve handles (e.g., VIPs) before general availability?
- How often should the blocklist reload to pick up hotfix edits without redeploying?
- Do we need audit logging for every reservation to meet compliance requirements?
