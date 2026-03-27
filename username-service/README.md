# Username Service

Spring Boot 4 (Java 25) service that exposes a REST API for generating Swedish-aware short usernames.

## Prerequisites

- Java 25 (OpenJDK)
- Maven 3.9+ (the wrapper `./mvnw` is checked in)
- Docker + Docker Compose v2 for local PostgreSQL

## Local Database via Docker

The app expects a PostgreSQL instance on `localhost:5432` with database `usernames`, username `username`, and password `secret`. A ready-made compose file lives in this directory.

```bash
# Start the database
docker compose -f docker-compose.dev.yml up -d postgres

# Check logs/health
docker compose -f docker-compose.dev.yml ps
docker compose -f docker-compose.dev.yml logs -f postgres

# Stop when finished
docker compose -f docker-compose.dev.yml down
```

The named volume `username-service-pgdata` keeps data between runs. Remove it with `docker volume rm username-service_pgdata` if you need a clean slate.

# Environment Variables

Configure sensitive values via `.env`. Copy `.env.example` to `.env`, update the values, and the Maven build (including `flyway:migrate`) will read them automatically:

```bash
cp .env.example .env
```

## Running the Application

```bash
cd username-service
docker compose -f docker-compose.dev.yml up -d postgres
export $(cat .env | xargs)
./mvnw spring-boot:run
```

Spring Boot will connect to the Dockerized PostgreSQL instance via the default properties in `src/main/resources/application.yml`. Override any setting with standard Spring config mechanisms (e.g., env vars like `SPRING_DATASOURCE_URL`).

Profiles:
- `dev` (default) – local Postgres + bundled blocklist file
- `staging` – enable with `SPRING_PROFILES_ACTIVE=staging`, reads `STAGING_DB_*` env vars and optional `BLOCKLIST_LOCATION`
- `prod` – enable with `SPRING_PROFILES_ACTIVE=prod`, reads `PROD_DB_*` env vars and optional `BLOCKLIST_LOCATION`

Health endpoints live at `/actuator/health` and `/actuator/health/readiness` (Spring Boot Actuator is enabled in every profile).

Once the service is running locally you can explore the OpenAPI UI at [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui); it renders the static spec served from `/v3/api-docs` (see `src/main/resources/openapi/openapi.json`).

### Database Schema & Flyway

Flyway runs automatically on startup, executing migrations in `src/main/resources/db/migration`. The initial script `V1__create_handles.sql` creates the `handles` table (`handle`, `email`, `created_at`). Run migrations manually when needed:

```bash
# ensure Postgres is running and env vars exported
./mvnw flyway:migrate
```

### Docker Image

```bash
cd username-service
docker build -t username-service:local .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e FLYWAY_URL=jdbc:postgresql://host.docker.internal:5432/usernames \
  -e FLYWAY_USER=username \
  -e FLYWAY_PASSWORD=secret \
  username-service:local
```

## Tests

Unit tests run against H2, so no Docker dependency:

```bash
./mvnw verify
```

To run the Postgres Testcontainers suite (`ReservationServiceIntegrationTest`), export `ENABLE_DOCKER_TESTS=true` and ensure Docker Desktop is running:

```bash
ENABLE_DOCKER_TESTS=true ./mvnw verify
```

Without that environment variable the integration suite is skipped to keep `./mvnw verify` fast on machines without Docker.

## Blocklist Maintenance CLI

Use the built-in CLI to deduplicate or append entries:

```bash
# list current entries
./mvnw -q exec:java -Dexec.mainClass=com.openspec.usernameservice.blocklist.BlocklistTool -- --list

# append a trigram
./mvnw -q exec:java -Dexec.mainClass=com.openspec.usernameservice.blocklist.BlocklistTool -- --add BOB

# operate on a custom file
./mvnw -q exec:java -Dexec.mainClass=com.openspec.usernameservice.blocklist.BlocklistTool -- --file /tmp/blocklist.json --add VIP
```

## Continuous Integration

`.github/workflows/ci.yml` runs on every push and pull request:
1. `./mvnw verify`
2. `ENABLE_DOCKER_TESTS=true ./mvnw verify`
3. Build + push Docker image to `ghcr.io/<owner>/username-service`
