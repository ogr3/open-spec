---
name: palantir-java-standard
description: Format Java code with Palantir Java Format before creating git commits.
license: MIT
compatibility: Requires Maven or Gradle formatter plugin configuration.
metadata:
  author: openspec
  version: "1.0"
---

Format Java code with the Palantir Java Format style before committing.

**When to use**
- Use this skill before `git commit` in Java repositories.
- Use this skill after code edits and before running final tests.

**Workflow**

1. **Detect build tool and formatter setup**
   - Check for `pom.xml`, `build.gradle`, or `build.gradle.kts`.
   - Confirm formatter plugin configuration exists:
     - Maven: Spotless (`spotless-maven-plugin`) with Palantir Java Format.
     - Gradle: Spotless plugin with `palantirJavaFormat()`.

2. **Run formatter**
   - Maven wrapper preferred:
     ```bash
     ./mvnw spotless:apply
     ```
   - Maven fallback:
     ```bash
     mvn spotless:apply
     ```
   - Gradle wrapper:
     ```bash
     ./gradlew spotlessApply
     ```

3. **Verify formatting is clean**
   - Run formatter check if configured:
     - Maven: `./mvnw spotless:check` (or `mvn spotless:check`)
     - Gradle: `./gradlew spotlessCheck`
   - If check fails, report blocking files and stop before commit.

4. **Proceed to commit flow**
   - Continue with tests and commit only after formatting and checks pass.

**If formatter is not configured**
- Do not guess formatter commands.
- Propose adding Spotless with Palantir Java Format to the build.
- Ask the user if you should add plugin configuration.

**Guardrails**
- Never use `--no-verify` to bypass hooks unless user explicitly asks.
- Keep formatting changes limited to Java formatting concerns.
- Prefer project wrappers (`./mvnw`, `./gradlew`) over global tools.
