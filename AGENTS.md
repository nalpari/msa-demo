# Repository Guidelines

## Memo
모든 답변과 추론과정은 한국어로 작성해 주세요.

## Project Structure & Module Organization
This repository contains four Spring Boot services: `config-server/`, `eureka/`, `gateway/`, and `test-service/`. Each follows the standard layout (`src/main/java`, `src/main/resources`, `src/test/java`) and uses the `com.interplug.<module>` package prefix for shared contracts. Configuration YAML files live under `src/main/resources`; profile-specific overrides should adopt the `application-<profile>.yml` pattern. Keep Git-backed secrets out of this repo and mirror config changes to the external Config Server repository.

## Build, Test, and Development Commands
Use `./gradlew clean build` to rebuild all modules and run verification. For focused tests, run module-scoped tasks such as `./gradlew :gateway:test`. Local development typically starts services in order: `:config-server:bootRun`, then `:eureka:bootRun`, `:gateway:bootRun`, and `:test-service:bootRun`. Always stop running servers once verification is complete.

## Coding Style & Naming Conventions
Source is Java 25 with Spring Boot 3.5.5 and Spring Cloud 2025.0.x. Follow 4-space indentation with braces on new lines and keep classes in UpperCamelCase while methods, fields, and variables use lowerCamelCase. REST endpoints adopt kebab-case paths (e.g., `/api/tests/search`). Favor Lombok annotations such as `@Slf4j` and `@RequiredArgsConstructor`, and prefer `log.info()` over `System.out`.

## Testing Guidelines
JUnit Jupiter is the default testing framework. Name test classes `<ClassName>Tests` and methods `shouldDo_X_whenY`. Maintain ≥80% line coverage on high-traffic paths like gateway filters and config refresh flows. Execute `./gradlew test` before commits, and document any coverage gaps or skipped scenarios in pull requests.

## Commit & Pull Request Guidelines
Commit messages are concise, present-tense, and often prefixed with the module (e.g., `gateway: add jwt auth filter`). For pull requests, describe affected services, list verification commands (`./gradlew clean build`), and capture API or configuration changes. Include screenshots or curl examples when endpoints change, and link relevant issues.

## Security & Configuration Tips
Local Config Server credentials (`devgrr/qwer1234`) are for development only; production secrets must live in the external config repo or environment vaults. Never commit generated certificates, JWT keys, or sensitive YAML. Document new environment variables or config expectations in the README so other contributors can bootstrap quickly.
