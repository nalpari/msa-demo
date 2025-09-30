# test-service Spring MVC → WebFlux 전환 계획

## 📋 현재 구조 분석

### 현재 기술 스택
- **Web Framework**: Spring MVC (`spring-boot-starter-web`)
- **데이터 접근**: Spring Data JPA (`spring-boot-starter-data-jpa`)
- **데이터베이스**: H2 (인메모리)
- **서비스간 통신**: OpenFeign Client
- **API 문서**: SpringDoc OpenAPI (WebMVC용)
- **Spring Boot**: 3.5.5
- **Java**: 24

### 주요 컴포넌트
- **Entity**: `Test` (JPA 기반)
- **Repository**: `TestRepository` (JpaRepository 상속)
- **Service**: `TestService` (동기식 트랜잭션)
- **Controller**: `TestController` (Spring MVC 기반)
- **Feign Client**: `UserServiceClient` (동기식 HTTP 통신)

---

## 🎯 전환 목표

**CRUD 기능 유지하면서 Spring MVC → WebFlux 전환**

### 변경 사항 요약
| 기존 (MVC) | 변경 후 (WebFlux) |
|------------|-------------------|
| spring-boot-starter-web | spring-boot-starter-webflux |
| spring-boot-starter-data-jpa | spring-boot-starter-data-r2dbc |
| H2 JDBC | H2 R2DBC (io.r2dbc:r2dbc-h2) |
| JpaRepository | R2dbcRepository |
| 동기식 Service | Reactive Service (Mono/Flux) |
| @RestController (blocking) | @RestController (reactive) |
| OpenFeign Client | WebClient |
| springdoc-openapi-starter-webmvc-ui | springdoc-openapi-starter-webflux-ui |

---

## 📝 Phase 1: 의존성 및 설정 변경

### 1.1 build.gradle 수정

**제거할 의존성**
```gradle
// 제거
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
implementation 'io.github.openfeign:feign-micrometer'
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

**추가할 의존성**
```gradle
// 추가
implementation 'org.springframework.boot:spring-boot-starter-webflux'
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
runtimeOnly 'io.r2dbc:r2dbc-h2'
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0'
```

⚠️ **주의**:
- Feign Client는 WebFlux와 호환되지 않으므로 WebClient로 완전 대체 필요
- H2 JDBC 드라이버는 제거하지 않아도 되지만, 사용하지 않으므로 제거 권장

---

### 1.2 application.yml 설정 변경

**기존 JPA/Datasource 설정 제거**
```yaml
# 제거할 설정
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**R2DBC 설정 추가**
```yaml
spring:
  r2dbc:
    url: r2dbc:h2:mem:///testdb
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
```

**스키마 초기화 파일 추가 필요**
- 파일 위치: `src/main/resources/schema.sql`
- R2DBC는 자동 DDL 생성이 없으므로 수동 작성 필요

```sql
-- schema.sql
CREATE TABLE IF NOT EXISTS tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## 📝 Phase 2: Entity 계층 변경

### 2.1 Test Entity 수정

**파일**: `src/main/java/com/interplug/testservice/entity/Test.java`

**변경 전 (JPA)**
```java
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tests")
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**변경 후 (R2DBC)**
```java
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table("tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
```

**주요 변경 포인트**:
- `jakarta.persistence.*` → `org.springframework.data.annotation.*`
- `@Entity` 제거 → `@Table("tests")` 사용
- `@GeneratedValue` 제거 (DB Auto Increment로 처리)
- `@CreationTimestamp`, `@UpdateTimestamp` 제거 (Service 계층에서 수동 설정)
- `@Column` 어노테이션은 선택사항 (snake_case 매핑 필요시 명시)

---

## 📝 Phase 3: Repository 계층 변경

### 3.1 TestRepository 변경

**파일**: `src/main/java/com/interplug/testservice/repository/TestRepository.java`

**변경 전 (JPA)**
```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByNameContaining(String keyword);
}
```

**변경 후 (R2DBC)**
```java
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TestRepository extends R2dbcRepository<Test, Long> {
    Flux<Test> findByNameContaining(String keyword);
}
```

**주요 변경 포인트**:
- `JpaRepository<Test, Long>` → `R2dbcRepository<Test, Long>`
- 반환 타입: `List<Test>` → `Flux<Test>`
- 단건 조회: `Optional<Test>` → `Mono<Test>` (상속된 메서드)

**R2dbcRepository 주요 메서드**:
- `Mono<T> save(T entity)` - 저장/수정
- `Mono<T> findById(ID id)` - ID로 조회
- `Flux<T> findAll()` - 전체 조회
- `Mono<Void> deleteById(ID id)` - ID로 삭제
- `Mono<Long> count()` - 개수 조회
- `Mono<Boolean> existsById(ID id)` - 존재 여부 확인

---

## 📝 Phase 4: Service 계층 Reactive 전환

### 4.1 TestService 변경

**파일**: `src/main/java/com/interplug/testservice/service/TestService.java`

**변경 전 (동기식)**
```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestService {
    private final TestRepository testRepository;

    @Transactional
    public TestDto create(TestDto testDto) {
        Test test = Test.builder()
                .name(testDto.getName())
                .description(testDto.getDescription())
                .build();
        test = testRepository.save(test);
        return convertToDto(test);
    }

    public TestDto findById(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));
        return convertToDto(test);
    }

    public List<TestDto> findAll() {
        return testRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
```

**변경 후 (Reactive)**
```java
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;

    public Mono<TestDto> create(TestDto testDto) {
        LocalDateTime now = LocalDateTime.now();
        Test test = Test.builder()
                .name(testDto.getName())
                .description(testDto.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return testRepository.save(test)
                .doOnSuccess(saved -> log.info("Created Test entity with ID: {}", saved.getId()))
                .map(this::convertToDto);
    }

    public Mono<TestDto> findById(Long id) {
        return testRepository.findById(id)
                .map(this::convertToDto)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Test not found with ID: " + id)
                ));
    }

    public Flux<TestDto> findAll() {
        return testRepository.findAll()
                .doOnComplete(() -> log.info("Retrieved all test entities"))
                .map(this::convertToDto);
    }

    public Flux<TestDto> findByNameContaining(String keyword) {
        return testRepository.findByNameContaining(keyword)
                .doOnComplete(() -> log.info("Search completed for keyword: {}", keyword))
                .map(this::convertToDto);
    }

    public Mono<TestDto> update(Long id, TestDto testDto) {
        return testRepository.findById(id)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Test not found with ID: " + id)
                ))
                .flatMap(test -> {
                    test.setName(testDto.getName());
                    test.setDescription(testDto.getDescription());
                    test.setUpdatedAt(LocalDateTime.now());
                    return testRepository.save(test);
                })
                .doOnSuccess(updated -> log.info("Updated Test entity with ID: {}", updated.getId()))
                .map(this::convertToDto);
    }

    public Mono<Void> delete(Long id) {
        return testRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Test not found with ID: " + id));
                    }
                    return testRepository.deleteById(id)
                            .doOnSuccess(v -> log.info("Deleted Test entity with ID: {}", id));
                });
    }

    private TestDto convertToDto(Test test) {
        return TestDto.builder()
                .id(test.getId())
                .name(test.getName())
                .description(test.getDescription())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
    }
}
```

**주요 변경 포인트**:
- `@Transactional` 제거 (R2DBC 트랜잭션은 별도 처리)
- 반환 타입: `TestDto` → `Mono<TestDto>`, `List<TestDto>` → `Flux<TestDto>`
- Stream API 제거 → Reactor Operators (`map`, `flatMap`, `switchIfEmpty`, `doOnSuccess`)
- 예외 처리: `orElseThrow()` → `switchIfEmpty(Mono.error())`
- Timestamp 수동 설정 (`LocalDateTime.now()`)
- 로깅: `doOnSuccess`, `doOnComplete` 활용

**Reactor Operators 설명**:
- `map()`: 데이터 변환 (1:1)
- `flatMap()`: 비동기 작업 체이닝 (1:N)
- `switchIfEmpty()`: 빈 결과 처리
- `doOnSuccess()`: 성공 시 side-effect (로깅 등)
- `doOnComplete()`: 완료 시 side-effect

---

## 📝 Phase 5: Controller WebFlux 전환

### 5.1 TestController 변경

**파일**: `src/main/java/com/interplug/testservice/controller/TestController.java`

**변경 전 (MVC)**
```java
@RestController
@RequestMapping("/api/tests")
public class TestController {
    public ResponseEntity<TestDto> createTest(@RequestBody TestDto testDto) {
        TestDto created = testService.create(testDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public ResponseEntity<List<TestDto>> getAllTests() {
        List<TestDto> tests = testService.findAll();
        return ResponseEntity.ok(tests);
    }
}
```

**변경 후 (WebFlux)**
```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Test Controller", description = "Test 리소스를 관리하는 API (Reactive)")
public class TestController {
    private final TestService testService;
    private final UserServiceClient userServiceClient;

    @PostMapping
    @Operation(summary = "Test 생성", description = "새로운 Test를 생성합니다")
    public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) {
        log.info("Creating new Test: {}", testDto.getName());
        return testService.create(testDto)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Test 조회", description = "ID로 Test를 조회합니다")
    public Mono<ResponseEntity<TestDto>> getTestById(@PathVariable Long id) {
        log.info("Fetching Test with ID: {}", id);
        return testService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @GetMapping
    @Operation(summary = "모든 Test 조회", description = "모든 Test 목록을 조회합니다")
    public Flux<TestDto> getAllTests() {
        log.info("Fetching all Tests");
        return testService.findAll();
    }

    @GetMapping("/search")
    @Operation(summary = "Test 검색", description = "키워드로 Test를 검색합니다")
    public Flux<TestDto> searchTests(@RequestParam String keyword) {
        log.info("Searching Tests with keyword: {}", keyword);
        return testService.findByNameContaining(keyword);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Test 수정", description = "기존 Test를 수정합니다")
    public Mono<ResponseEntity<TestDto>> updateTest(
            @PathVariable Long id,
            @RequestBody TestDto testDto) {
        log.info("Updating Test with ID: {}", id);
        return testService.update(id, testDto)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Test 삭제", description = "Test를 삭제합니다")
    public Mono<ResponseEntity<Void>> deleteTest(@PathVariable Long id) {
        log.info("Deleting Test with ID: {}", id);
        return testService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    // Feign Client → WebClient 통신 예제
    @GetMapping("/{testId}/user/{userId}")
    @Operation(summary = "Test와 User 조회", description = "Test와 연관된 User 정보를 함께 조회합니다 (WebClient 사용)")
    public Mono<ResponseEntity<UserDto>> getTestWithUser(
            @PathVariable Long testId,
            @PathVariable Long userId) {
        log.info("Fetching Test {} with User {}", testId, userId);

        return testService.findById(testId)
                .doOnNext(test -> log.info("Found Test: {}", test.getName()))
                .flatMap(test -> userServiceClient.getUserById(userId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching test or user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/users")
    @Operation(summary = "모든 User 조회", description = "WebClient를 통해 User 서비스의 모든 사용자를 조회합니다")
    public Flux<UserDto> getAllUsersViaWebClient() {
        log.info("Fetching all users via WebClient");
        return userServiceClient.getAllUsers()
                .doOnComplete(() -> log.info("Completed fetching users"));
    }
}
```

**주요 변경 포인트**:
- 반환 타입: `ResponseEntity<T>` → `Mono<ResponseEntity<T>>`
- 컬렉션 반환: `ResponseEntity<List<T>>` → `Flux<T>` (직접 반환 권장)
- 에러 처리: `try-catch` → `onErrorResume`
- 비동기 체이닝: `map`, `flatMap`, `then` 활용
- Feign 호출 → WebClient 호출 (다음 단계에서 구현)

**권장 반환 타입**:
- 단건 조회: `Mono<ResponseEntity<T>>` 또는 `Mono<T>`
- 다건 조회: `Flux<T>` (ResponseEntity 래핑 불필요)
- 삭제/수정: `Mono<ResponseEntity<Void>>` 또는 `Mono<Void>`

---

## 📝 Phase 6: Feign Client → WebClient 전환

### 6.1 파일 삭제
- `UserServiceClient.java` (Feign 인터페이스)
- `UserServiceFallback.java` (Fallback 구현)
- `FeignConfig.java` (Feign 설정)

### 6.2 WebClient 기반 서비스 생성

**신규 파일**: `src/main/java/com/interplug/testservice/client/UserServiceClient.java`

```java
package com.interplug.testservice.client;

import com.interplug.testservice.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Service
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/users")  // Gateway를 통한 라우팅
                // 또는 Eureka 연동: .baseUrl("http://user-service/api/users")
                .build();
    }

    /**
     * User ID로 사용자 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 Mono (없으면 empty)
     */
    public Mono<UserDto> getUserById(Long userId) {
        return webClient.get()
                .uri("/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", user != null ? user.getName() : "null"))
                .onErrorResume(e -> {
                    log.error("Error fetching user {}: {}", userId, e.getMessage());
                    return Mono.empty();  // Fallback: 빈 결과 반환
                });
    }

    /**
     * 모든 사용자 조회
     * @return 사용자 목록 Flux
     */
    public Flux<UserDto> getAllUsers() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(UserDto.class)
                .timeout(Duration.ofSeconds(5))
                .doOnComplete(() -> log.info("Successfully fetched all users"))
                .onErrorResume(e -> {
                    log.error("Error fetching all users: {}", e.getMessage());
                    return Flux.empty();  // Fallback: 빈 목록 반환
                });
    }
}
```

**주요 변경 포인트**:
- `@FeignClient` 제거 → `@Service` + `WebClient`
- 동기 호출 → Reactive 비동기 호출 (`Mono<T>`, `Flux<T>`)
- Fallback: `@FeignClient(fallback = ...)` → `onErrorResume()`
- Timeout: 명시적 설정 필요 (`timeout()`)
- Circuit Breaker는 별도 Resilience4j 설정 필요 (선택사항)

**WebClient 설정 옵션**:
```java
// Gateway를 통한 라우팅 (권장)
.baseUrl("http://localhost:8080/api/users")

// Eureka를 통한 서비스 디스커버리
.baseUrl("http://user-service/api/users")
```

---

## 📝 Phase 7: 설정 및 기타 구성요소 변경

### 7.1 SwaggerConfig 수정

**파일**: `src/main/java/com/interplug/testservice/config/SwaggerConfig.java`

**기존 설정 유지 가능**, 의존성만 변경되면 WebFlux와 호환됨.

```java
package com.interplug.testservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Test Service API (Reactive)")
                        .version("2.0.0")
                        .description("Spring WebFlux 기반 반응형 API")
                        .contact(new Contact()
                                .name("Interplug Team")
                                .email("support@interplug.com")));
    }
}
```

**Swagger UI 접근**: `http://localhost:{port}/swagger-ui.html`

---

### 7.2 DataInitializer 수정

**파일**: `src/main/java/com/interplug/testservice/config/DataInitializer.java`

**변경 전 (JPA)**
```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final TestRepository testRepository;

    @Override
    public void run(String... args) throws Exception {
        Test test1 = Test.builder()
                .name("Sample Test 1")
                .description("This is a sample test")
                .build();
        testRepository.save(test1);
    }
}
```

**변경 후 (R2DBC)**
```java
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final TestRepository testRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        testRepository.deleteAll()
                .thenMany(Flux.just(
                        Test.builder()
                                .name("Sample Test 1")
                                .description("This is a sample test")
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        Test.builder()
                                .name("Sample Test 2")
                                .description("Another sample test")
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ))
                .flatMap(testRepository::save)
                .doOnNext(test -> log.info("Initialized Test: {}", test.getName()))
                .doOnComplete(() -> log.info("Test data initialization completed"))
                .subscribe();  // 또는 .blockLast() (초기화 완료 대기)
    }
}
```

**주요 변경 포인트**:
- `CommandLineRunner` → `ApplicationRunner`
- 동기 저장 → Reactive 저장 (`Flux`, `flatMap`)
- `subscribe()` 호출 필요 (또는 `blockLast()`)
- Timestamp 수동 설정

⚠️ **주의**: `subscribe()`는 비동기 실행이므로 초기화가 완료되기 전에 애플리케이션이 실행될 수 있음. 확실한 초기화를 위해 `blockLast()` 사용 권장.

```java
.blockLast();  // 초기화 완료까지 대기
```

---

### 7.3 Application 메인 클래스 수정

**파일**: `src/main/java/com/interplug/testservice/TestServiceApplication.java`

**변경 전**
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestServiceApplication.class, args);
    }
}
```

**변경 후**
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories  // R2DBC 활성화
public class TestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestServiceApplication.class, args);
    }
}
```

**주요 변경 포인트**:
- `@EnableFeignClients` 제거
- `@EnableR2dbcRepositories` 추가 (선택사항, 자동 감지됨)

---

## 📝 Phase 8: 스키마 파일 생성

### 8.1 schema.sql 생성

**파일**: `src/main/resources/schema.sql`

```sql
DROP TABLE IF EXISTS tests;

CREATE TABLE tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**application.yml에 설정 추가**
```yaml
spring:
  sql:
    init:
      mode: always  # 항상 스키마 실행
      schema-locations: classpath:schema.sql
```

⚠️ **주의**: `mode: always`는 개발 환경에서만 사용. 운영 환경에서는 `mode: never` 또는 Flyway/Liquibase 사용 권장.

---

## 📊 전환 작업 체크리스트

### ✅ Phase 1: 환경 설정
- [ ] build.gradle 의존성 변경
  - [ ] spring-boot-starter-web → webflux
  - [ ] spring-boot-starter-data-jpa → data-r2dbc
  - [ ] h2 → r2dbc-h2
  - [ ] openfeign 제거
  - [ ] springdoc-openapi webmvc → webflux
- [ ] application.yml R2DBC 설정
- [ ] schema.sql 파일 생성
- [ ] 빌드 테스트: `./gradlew clean build`

### ✅ Phase 2: 데이터 계층
- [ ] Test Entity R2DBC 호환 변경
  - [ ] JPA 어노테이션 제거
  - [ ] R2DBC 어노테이션 추가
  - [ ] Timestamp 자동 생성 제거
- [ ] TestRepository 인터페이스 변경
  - [ ] JpaRepository → R2dbcRepository
  - [ ] 반환 타입 Mono/Flux 변경
- [ ] 컴파일 오류 해결

### ✅ Phase 3: 비즈니스 계층
- [ ] TestService 메서드 Reactive 전환
  - [ ] 모든 메서드 반환 타입 변경
  - [ ] @Transactional 제거
  - [ ] Reactor Operators 적용
  - [ ] Timestamp 수동 처리
  - [ ] 에러 처리 Reactive 방식 변경
- [ ] 로깅 개선 (doOnSuccess, doOnComplete)

### ✅ Phase 4: 프레젠테이션 계층
- [ ] TestController Reactive 전환
  - [ ] 모든 엔드포인트 반환 타입 변경
  - [ ] ResponseEntity 처리 변경
  - [ ] 에러 핸들링 onErrorResume 적용
- [ ] Swagger 어노테이션 검증

### ✅ Phase 5: 외부 통신
- [ ] Feign Client 파일 삭제
  - [ ] UserServiceClient.java
  - [ ] UserServiceFallback.java
  - [ ] FeignConfig.java
- [ ] WebClient 기반 UserServiceClient 생성
  - [ ] baseUrl 설정 (Gateway 또는 Eureka)
  - [ ] Timeout 설정
  - [ ] Fallback 로직 구현
- [ ] Controller에서 WebClient 사용 확인

### ✅ Phase 6: 설정 및 초기화
- [ ] SwaggerConfig 확인
- [ ] DataInitializer Reactive 변환
  - [ ] ApplicationRunner로 변경
  - [ ] Reactive 저장 로직 적용
  - [ ] blockLast() 추가 (초기화 대기)
- [ ] TestServiceApplication 수정
  - [ ] @EnableFeignClients 제거
  - [ ] @EnableR2dbcRepositories 추가

### ✅ Phase 7: 통합 테스트
- [ ] 애플리케이션 시작 확인
- [ ] H2 Console 접근 확인
- [ ] Swagger UI 접근 확인
- [ ] CRUD API 테스트
  - [ ] POST /api/tests (생성)
  - [ ] GET /api/tests (전체 조회)
  - [ ] GET /api/tests/{id} (단건 조회)
  - [ ] GET /api/tests/search?keyword= (검색)
  - [ ] PUT /api/tests/{id} (수정)
  - [ ] DELETE /api/tests/{id} (삭제)
- [ ] 마이크로서비스 통신 테스트
  - [ ] GET /api/tests/{testId}/user/{userId}
  - [ ] GET /api/tests/users
- [ ] 에러 처리 검증 (존재하지 않는 ID 등)
- [ ] 성능 테스트 (선택사항)

---

## ⚠️ 주의사항 및 트러블슈팅

### 1. H2 Database 스키마 문제
**문제**: R2DBC는 자동 DDL 생성 안됨
**해결**: `schema.sql` 수동 작성 필수

### 2. Timestamp 자동 생성 제거
**문제**: `@CreationTimestamp`, `@UpdateTimestamp` 미지원
**해결**: Service 계층에서 `LocalDateTime.now()` 수동 설정

### 3. 트랜잭션 관리 변경
**문제**: `@Transactional` 동작 방식 다름
**해결**: 필요시 `TransactionalOperator` 사용
```java
@Bean
public TransactionalOperator transactionalOperator(ReactiveTransactionManager txManager) {
    return TransactionalOperator.create(txManager);
}
```

### 4. Blocking 코드 호출 금지
**문제**: Reactive Stream 내에서 `.block()` 호출 시 성능 저하
**해결**: Reactive chain 유지, 초기화 시점에만 `.blockLast()` 허용

### 5. Feign Client 의존성 충돌
**문제**: WebFlux와 Feign 의존성 충돌 가능
**해결**: build.gradle에서 Feign 관련 의존성 완전 제거 필수

### 6. WebClient baseUrl 설정
**문제**: 마이크로서비스 간 통신 URL 설정
**해결**:
- 개발 환경: `http://localhost:8080/api/users` (Gateway)
- Eureka 연동: `http://user-service/api/users`

### 7. H2 Console WebFlux 환경
**문제**: WebFlux에서 H2 Console 동작 안될 수 있음
**해결**: 개발 환경에서만 사용, 운영 환경은 외부 DB 사용

### 8. Exception Handling
**문제**: 기존 `@ControllerAdvice` 동작 방식 다를 수 있음
**해결**: WebFlux용 에러 핸들러 추가
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<String>> handleRuntimeException(RuntimeException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }
}
```

---

## 💡 예상 소요 시간

| Phase | 작업 내용 | 예상 시간 |
|-------|----------|-----------|
| Phase 1-2 | 설정 + Entity/Repository 변경 | 1-2시간 |
| Phase 3 | Service 계층 Reactive 전환 | 2-3시간 |
| Phase 4 | Controller 전환 | 1-2시간 |
| Phase 5 | Feign → WebClient 전환 | 1-2시간 |
| Phase 6-7 | 설정 및 초기화 + 테스트 | 1-2시간 |
| Phase 8 | 버그 수정 및 최적화 | 1-2시간 |

**총 예상 시간: 7-13시간**

---

## 📚 참고 자료

### Spring WebFlux 공식 문서
- https://docs.spring.io/spring-framework/reference/web/webflux.html

### Spring Data R2DBC 공식 문서
- https://spring.io/projects/spring-data-r2dbc

### Reactor Core 문서
- https://projectreactor.io/docs/core/release/reference/

### 주요 Reactor Operators
- `map()`: 데이터 변환
- `flatMap()`: 비동기 작업 체이닝
- `switchIfEmpty()`: 빈 결과 처리
- `doOnSuccess()`, `doOnComplete()`: Side-effect
- `onErrorResume()`: 에러 처리 및 Fallback
- `timeout()`: Timeout 설정

---

## 🚀 전환 후 검증 방법

### 1. 애플리케이션 시작 확인
```bash
cd test-service
./gradlew clean build
./gradlew bootRun
```

### 2. Swagger UI 접근
```
http://localhost:{port}/swagger-ui.html
```

### 3. CRUD API 테스트 (curl)
```bash
# 생성
curl -X POST http://localhost:{port}/api/tests \
  -H "Content-Type: application/json" \
  -d '{"name":"Test 1","description":"Sample"}'

# 전체 조회
curl http://localhost:{port}/api/tests

# 단건 조회
curl http://localhost:{port}/api/tests/1

# 검색
curl http://localhost:{port}/api/tests/search?keyword=Test

# 수정
curl -X PUT http://localhost:{port}/api/tests/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Test","description":"Updated"}'

# 삭제
curl -X DELETE http://localhost:{port}/api/tests/1
```

### 4. WebClient 통신 테스트
```bash
# User 서비스 연동 테스트
curl http://localhost:{port}/api/tests/1/user/1
curl http://localhost:{port}/api/tests/users
```

---

## 📝 전환 완료 후 체크포인트

✅ **성공 기준**:
- [ ] 애플리케이션 정상 시작 (에러 없음)
- [ ] 모든 CRUD API 정상 동작
- [ ] Swagger UI 접근 및 API 문서 확인
- [ ] 마이크로서비스 간 통신 정상 (WebClient)
- [ ] 에러 처리 정상 동작
- [ ] 로그 정상 출력
- [ ] 데이터 초기화 정상 동작

✅ **성능 검증** (선택사항):
- [ ] 동시성 테스트 (부하 테스트)
- [ ] 응답 시간 측정
- [ ] 메모리 사용량 확인

---

## 결론

이 문서는 test-service 모듈을 Spring MVC에서 WebFlux로 전환하기 위한 체계적인 계획입니다. 각 Phase별로 순차적으로 진행하며, 체크리스트를 활용하여 누락 없이 전환 작업을 완료하시기 바랍니다.

**주요 변경 포인트 요약**:
1. 의존성: MVC → WebFlux, JPA → R2DBC
2. Repository: JpaRepository → R2dbcRepository
3. Service: 동기식 → Reactive (Mono/Flux)
4. Controller: Blocking → Non-blocking
5. 외부 통신: Feign → WebClient
6. 설정: Datasource → R2DBC 설정

전환 작업 중 문제가 발생하면 트러블슈팅 섹션을 참고하시기 바랍니다.