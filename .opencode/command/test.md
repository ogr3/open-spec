---
description: Run test suite with coverage
---

Run the project's tests with coverage enabled.

Run the full test suite with coverage report and show any failures.
Focus on the failing tests and suggest fixes.

Steps:

1. Detect the repository's test command from common tools (`package.json`, `pyproject.toml`, `go.mod`, etc.).
2. Run the appropriate coverage command.
   - JavaScript/TypeScript (npm): `npm test -- --coverage`
   - JavaScript/TypeScript (bun): `bun test --coverage`
   - Python (pytest): `pytest --cov`
   - Go: `go test ./... -cover`
   - Java (Maven): `mvn test jacoco:report`
   - Java (Gradle): `./gradlew test jacocoTestReport`
3. Report a concise summary including:
   - whether tests passed or failed
   - total tests run (if available)
   - coverage percentage (if available)
   - any failing test names/errors

If the test tool cannot be determined automatically, ask the user which command to use.
