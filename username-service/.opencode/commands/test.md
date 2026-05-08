---
description: Run test suite with coverage
---

Run the project's tests with coverage enabled.

Run the full test suite with coverage report and show any failures.
Focus on the failing tests and suggest fixes.

For Java projects, run this as a `code-reviewer` style execution:
- run tests with coverage
- report pass/fail, totals, skipped tests, and coverage
- identify risks (e.g., env-gated skips, JDK warnings)
- suggest concrete fixes

Steps:

1. Detect the repository's test command from common tools (`package.json`, `pyproject.toml`, `go.mod`, etc.).
2. Run the appropriate coverage command.
   - JavaScript/TypeScript (npm): `npm test -- --coverage`
   - JavaScript/TypeScript (bun): `bun test --coverage`
   - Python (pytest): `pytest --cov`
   - Go: `go test ./... -cover`
   - Java (Maven): prefer `./mvnw test jacoco:report`, fallback `mvn test jacoco:report`
   - Java (Gradle): prefer `./gradlew test jacocoTestReport`, fallback `gradle test jacocoTestReport`
3. Report a concise summary including:
   - whether tests passed or failed
   - total tests run (if available)
   - coverage percentage (if available)
   - any failing test names/errors
   - key warnings or skipped-test reasons (especially Java integration tests)

Java-specific output requirements:
- include command actually run
- list skipped tests with reason
- include instruction and line coverage when JaCoCo is available
- call out JDK/runtime warnings that may become future failures

If the test tool cannot be determined automatically, ask the user which command to use.
