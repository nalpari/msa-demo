# Test Service

Spring WebFlux 기반 반응형 마이크로서비스

## 목차
- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [데이터베이스](#데이터베이스)
- [설정](#설정)
- [실행 방법](#실행-방법)
- [API 테스트](#api-테스트)
- [Swagger UI](#swagger-ui)
- [트러블슈팅](#트러블슈팅)

---

## 개요

test-service는 Spring WebFlux와 R2DBC를 사용하는 반응형(Reactive) 마이크로서비스입니다. H2 인메모리 데이터베이스를 사용하여 간단한 CRUD 작업을 수행하며, 다른 마이크로서비스와의 통신을 위해 WebClient를 활용합니다.

### 주요 특징
- **Spring WebFlux**: 비동기 논블로킹 웹 프레임워크
- **R2DBC**: 반응형 데이터베이스 드라이버 (H2)
- **Eureka Client**: 서비스 디스커버리
- **WebClient**: 비동기 HTTP 클라이언트
- **Swagger UI**: API 문서화 (WebFlux 최적화)

---

## 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.5.5
- **Spring Cloud**: 2025.0.0
- **반응형 스택**: Spring WebFlux, Reactor Core
- **데이터베이스**: H2 (R2DBC)
- **빌드 도구**: Gradle

### 주요 의존성
```gradle
dependencies {
    // Core
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'

    // Spring Cloud
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'

    // Database
    runtimeOnly 'io.r2dbc:r2dbc-h2'

    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0'

    // Development
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

---

## 프로젝트 구조

```
test-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.interplug.testservice/
│   │   │       ├── TestServiceApplication.java
│   │   │       ├── client/
│   │   │       │   └── UserServiceClient.java        # WebClient 기반 외부 서비스 통신
│   │   │       ├── config/
│   │   │       │   ├── DataInitializer.java          # 초기 데이터 로딩
│   │   │       │   └── SwaggerConfig.java            # Swagger 설정
│   │   │       ├── controller/
│   │   │       │   └── TestController.java           # REST API 컨트롤러
│   │   │       ├── dto/
│   │   │       │   ├── TestDto.java                  # Test DTO
│   │   │       │   └── UserDto.java                  # User DTO
│   │   │       ├── entity/
│   │   │       │   └── Test.java                     # Test 엔티티
│   │   │       ├── repository/
│   │   │       │   └── TestRepository.java           # R2DBC Repository
│   │   │       └── service/
│   │   │           └── TestService.java              # 비즈니스 로직
│   │   └── resources/
│   │       ├── application.yml                       # 애플리케이션 설정
│   │       └── schema.sql                           # 테이블 스키마
│   └── test/
│       └── java/
│           └── com.interplug.testservice/
│               └── TestServiceApplicationTests.java
└── build.gradle
```

---

## 주요 기능

### 1. CRUD 작업
- Test 엔티티의 생성, 조회, 수정, 삭제 기능
- 반응형 프로그래밍 모델 (Mono/Flux) 활용

### 2. 검색 기능
- 키워드 기반 Test 이름 검색 (`findByNameContaining`)

### 3. 외부 서비스 통신
- WebClient를 사용한 다른 마이크로서비스와의 통신
- User 서비스 연동 예제 포함

### 4. API 문서화
- SpringDoc OpenAPI를 통한 자동 API 문서화
- Swagger UI 제공

### 5. 서비스 디스커버리
- Eureka Client 통합
- 동적 서비스 등록 및 탐색

---

## API 엔드포인트

### Base URL
- **개발**: `http://localhost:{dynamic-port}/api/tests`
- **Eureka 통한 접근**: `http://test-service/api/tests`

### Test 리소스 관리

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/tests` | Test 생성 | TestDto | TestDto (201) |
| GET | `/api/tests/{id}` | Test 조회 (ID) | - | TestDto (200) |
| GET | `/api/tests` | 전체 Test 조회 | - | Flux\<TestDto\> (200) |
| GET | `/api/tests/search?keyword={keyword}` | Test 검색 | - | Flux\<TestDto\> (200) |
| PUT | `/api/tests/{id}` | Test 수정 | TestDto | TestDto (200) |
| DELETE | `/api/tests/{id}` | Test 삭제 | - | Void (204) |

### WebClient 통신 예제

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/tests/{testId}/user/{userId}` | Test와 User 정보 함께 조회 | UserDto (200) |
| GET | `/api/tests/users` | 모든 User 조회 (WebClient) | Flux\<UserDto\> (200) |

---

## 데이터베이스

### H2 인메모리 데이터베이스

#### 연결 설정
```yaml
spring:
  r2dbc:
    url: r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
```

#### 테이블 스키마 (schema.sql)
```sql
CREATE TABLE IF NOT EXISTS tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Entity 정의
```java
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

---

## 설정

### application.yml

```yaml
# 서버 설정
server:
  port: 0  # Random port (Eureka에서 동적 할당)

# Eureka Client 설정
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka

# Spring 설정
spring:
  application:
    name: test-service

  # R2DBC 설정 (WebFlux용)
  r2dbc:
    url: r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:

  # H2 콘솔 설정
  h2:
    console:
      enabled: true
      path: /h2-console

  # SQL 스키마 초기화
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

# 로깅 설정
logging:
  level:
    com.interplug.testservice.client: DEBUG
    org.springframework.r2dbc: DEBUG

# Swagger UI 설정
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
  api-docs:
    path: /v3/api-docs
    enabled: true
```

---

## 실행 방법

### 사전 요구사항

1. **Java 21 이상** 설치
2. **Eureka Server** 실행 (선택적)
   ```bash
   cd ../eureka && ./gradlew bootRun
   ```

### 단계별 실행

#### 1. 빌드
```bash
cd test-service
./gradlew clean build
```

#### 2. 실행
```bash
# Gradle을 통한 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/test-service-0.0.1-SNAPSHOT.jar
```

#### 3. 실행 확인
```bash
# Health Check
curl http://localhost:{port}/actuator/health

# Eureka 등록 확인
http://localhost:8761
```

---

## API 테스트

### 1. Test 생성
```bash
curl -X POST http://localhost:{port}/api/tests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 이름",
    "description": "테스트 설명입니다"
  }'
```

**Response (201 Created)**:
```json
{
  "id": 1,
  "name": "테스트 이름",
  "description": "테스트 설명입니다",
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00"
}
```

### 2. Test 조회
```bash
# 단건 조회
curl http://localhost:{port}/api/tests/1

# 전체 조회
curl http://localhost:{port}/api/tests

# 검색
curl http://localhost:{port}/api/tests/search?keyword=테스트
```

### 3. Test 수정
```bash
curl -X PUT http://localhost:{port}/api/tests/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "수정된 이름",
    "description": "수정된 설명입니다"
  }'
```

### 4. Test 삭제
```bash
curl -X DELETE http://localhost:{port}/api/tests/1
```

**Response (204 No Content)**

### 5. WebClient 통신 테스트
```bash
# Test와 User 정보 함께 조회
curl http://localhost:{port}/api/tests/1/user/1

# 모든 User 조회 (WebClient를 통해)
curl http://localhost:{port}/api/tests/users
```

---

## Swagger UI

### 접근 방법

1. **Swagger UI**
   ```
   http://localhost:{port}/swagger-ui.html
   ```

2. **OpenAPI 문서**
   ```
   http://localhost:{port}/v3/api-docs
   ```

### Swagger 화면에서 API 테스트

1. Swagger UI 접속
2. 원하는 API 엔드포인트 선택
3. `Try it out` 버튼 클릭
4. 필요한 파라미터 입력
5. `Execute` 버튼 클릭하여 API 호출

---

## 반응형 프로그래밍 예제

### Service Layer (Mono/Flux 활용)

```java
@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;

    // 단건 조회 → Mono
    public Mono<TestDto> findById(Long id) {
        return testRepository.findById(id)
                .map(this::convertToDto)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Test not found with ID: " + id)
                ));
    }

    // 복수 조회 → Flux
    public Flux<TestDto> findAll() {
        return testRepository.findAll()
                .doOnComplete(() -> log.info("Retrieved all test entities"))
                .map(this::convertToDto);
    }

    // 생성
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
}
```

### Controller Layer (Reactive Endpoints)

```java
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @PostMapping
    public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) {
        return testService.create(testDto)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping
    public Flux<TestDto> getAllTests() {
        return testService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TestDto>> getTestById(@PathVariable Long id) {
        return testService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }
}
```

### WebClient 사용 예제

```java
@Service
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/users")  // Gateway를 통한 라우팅
                .build();
    }

    public Mono<UserDto> getUserById(Long userId) {
        return webClient.get()
                .uri("/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getName()))
                .onErrorResume(e -> {
                    log.error("Error fetching user {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<UserDto> getAllUsers() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(UserDto.class)
                .timeout(Duration.ofSeconds(5))
                .doOnComplete(() -> log.info("Successfully fetched all users"))
                .onErrorResume(e -> {
                    log.error("Error fetching all users: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}
```

---

## 트러블슈팅

### 일반적인 문제 해결

#### 1. 포트 충돌
**문제**: "Port already in use" 에러
**해결**: test-service는 `server.port: 0`으로 설정되어 랜덤 포트를 사용합니다. Eureka 대시보드에서 실제 포트를 확인하세요.

```bash
# Eureka 대시보드 확인
http://localhost:8761
```

#### 2. H2 데이터베이스 연결 실패
**문제**: R2DBC 연결 오류
**해결**:
```yaml
# application.yml 확인
spring:
  r2dbc:
    url: r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
```

#### 3. Eureka 등록 실패
**문제**: 서비스가 Eureka에 등록되지 않음
**해결**:
```bash
# 1. Eureka Server 실행 확인
http://localhost:8761

# 2. application.yml 확인
eureka:
  client:
    register-with-eureka: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

#### 4. WebClient 통신 실패
**문제**: User 서비스 호출 실패 (503 Service Unavailable)
**해결**:
- User 서비스가 실행 중인지 확인
- Gateway를 통한 라우팅 경로 확인
- Timeout 설정 확인 (기본 3초)

```java
.timeout(Duration.ofSeconds(3))
.onErrorResume(e -> {
    log.error("Error: {}", e.getMessage());
    return Mono.empty();  // Fallback
});
```

#### 5. 반응형 스트림 블로킹
**문제**: WebFlux에서 `.block()` 사용으로 인한 에러
**해결**: `.block()` 대신 반응형 체이닝 사용

```java
// ❌ Bad
public TestDto getTest(Long id) {
    return testRepository.findById(id).block();  // 절대 사용 금지
}

// ✅ Good
public Mono<TestDto> getTest(Long id) {
    return testRepository.findById(id)
            .map(this::convertToDto);
}
```

---

## 개발 가이드

### 코딩 컨벤션
- [코딩 컨벤션 가이드](../docs/CONVENTIONS.md) 참조

### 주요 원칙
1. **반응형 프로그래밍**: 모든 I/O 작업은 비동기 처리
2. **에러 핸들링**: `switchIfEmpty`, `onErrorResume` 적극 활용
3. **로깅**: `doOnSuccess`, `doOnComplete`, `doOnError` 사용
4. **Null 안전성**: Java Optional 또는 Mono.empty() 사용

### 테스트 작성
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestServiceApplicationTests {

    @Autowired
    private TestRepository testRepository;

    @Test
    public void testCreateAndFind() {
        Test test = Test.builder()
                .name("Test Name")
                .description("Test Description")
                .build();

        StepVerifier.create(testRepository.save(test))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("Test Name", saved.getName());
                })
                .verifyComplete();
    }
}
```

---

## 참고 자료

### 공식 문서
- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/3.5.5/reference/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [SpringDoc OpenAPI](https://springdoc.org/)

### 관련 문서
- [메인 프로젝트 CLAUDE.md](../CLAUDE.md) - MSA 전체 구조
- [코딩 컨벤션 가이드](../docs/CONVENTIONS.md) - 코딩 표준

---

## 라이선스

Copyright © 2025 Interplug. All rights reserved.

---

## 버전 정보

- **Version**: 0.0.1-SNAPSHOT
- **Java**: 21
- **Spring Boot**: 3.5.5
- **Spring Cloud**: 2025.0.0
- **Last Updated**: 2025-10-10
