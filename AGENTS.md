# Repository Guidelines

## Memo
- 모든 답변과 추론과정은 한국어로 보여주세요.
- 이 저장소는 Spring Boot 3.5.5, Spring Cloud 2025.0.x, Java 25를 사용합니다.
- 기능 검증 및 테스트를 위해서 서버를 실행할 경우 태스크가 종료될때 반드시 서버를 종료 해주세요.

## Project Structure & Module Organization
- `config-server/` provides centralized Spring Cloud Config; keep Git-backed secrets out of this repo and mirror updates to the external config repository.
- `eureka/`, `gateway/`, and `test-service/` each follow the Spring Boot layout (`src/main/java`, `src/main/resources`, `src/test/java`); place shared contracts in the `com.interplug` package hierarchy that matches the module name.
- Top-level `build.gradle` controls common dependency management and Java toolchain 25; module-specific overrides live in each subproject `build.gradle`.

## Build, Test, and Development Commands
- `./gradlew clean build` rebuilds every service and runs all verification tasks; run before pushing.
- `./gradlew test` executes the full JUnit 5 suite; use `./gradlew :gateway:test` style commands for scoped runs.
- `./gradlew :config-server:bootRun` → `:eureka:bootRun` → `:gateway:bootRun` → `:test-service:bootRun` keeps the dependency chain happy during local development.

## Coding Style & Naming Conventions
- Use 4-space indentation, brace-on-new-line Java style, and keep classes in `UpperCamelCase`, methods/fields in `lowerCamelCase`, and REST endpoints kebab-case (e.g., `/api/tests/search`).
- Favor Lombok annotations already in use (`@Slf4j`, `@RequiredArgsConstructor`) to reduce boilerplate; log with `log.info()` rather than `System.out`.
- Configuration belongs in YAML under `src/main/resources`, with profile-specific overrides using Spring profile suffixes (e.g., `application-dev.yml`).

## Testing Guidelines
- Default to JUnit Jupiter with Spring Boot slices; convert placeholder context tests into focused service/repository tests using Mockito where external calls (Feign clients or repositories) are involved.
- Name test classes `<ClassName>Tests` and methods in `shouldDo_X_whenY` form for clarity.
- Maintain high-traffic paths (gateway filters, config refresh) at ≥80% line coverage and document gaps in the PR description when lower.

## Commit & Pull Request Guidelines
- Git history currently shows short, lowercase imperative messages (e.g., `initial commit`); continue with concise, present-tense summaries and prefix the touched module when helpful (`gateway: add jwt auth filter`).
- For pull requests, describe the service(s) affected, list run commands (`./gradlew clean build`), and capture any config or security implications; attach screenshots or curl examples when API contracts change.

## Security & Configuration Notes
- Config Server credentials (`devgrr/qwer1234`) are for local use only—store production secrets in the external Git repo or environment vaults.
- Never commit generated certificates, JWT keys, or sensitive YAML; reference them via environment variables and document the expectation in `README.md` if new settings are required.
