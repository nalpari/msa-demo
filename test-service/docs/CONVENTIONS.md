# 코딩 컨벤션 가이드

## 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [패키지 구조](#패키지-구조)
3. [네이밍 컨벤션](#네이밍-컨벤션)
4. [코드 스타일](#코드-스타일)
5. [어노테이션 규칙](#어노테이션-규칙)
6. [반응형 프로그래밍](#반응형-프로그래밍)
7. [API 설계](#api-설계)
8. [에러 핸들링](#에러-핸들링)
9. [로깅](#로깅)
10. [의존성 주입](#의존성-주입)

---

## 프로젝트 구조

### 기본 구조
```
test-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.interplug.testservice/
│   │   │       ├── client/          # 외부 서비스 통신 클라이언트
│   │   │       ├── config/          # 설정 클래스
│   │   │       ├── controller/      # REST 컨트롤러
│   │   │       ├── dto/             # 데이터 전송 객체
│   │   │       ├── entity/          # 엔티티 클래스
│   │   │       ├── repository/      # 리포지토리 인터페이스
│   │   │       ├── service/         # 비즈니스 로직
│   │   │       └── TestServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml      # 설정 파일
│   │       └── schema.sql          # DB 스키마
│   └── test/
└── build.gradle
```

---

## 패키지 구조

### 계층별 역할 정의

| 패키지 | 역할 | 명명 규칙 |
|--------|------|-----------|
| `client` | 외부 서비스 통신 | `*Client.java` |
| `config` | 설정 및 Bean 정의 | `*Config.java` |
| `controller` | HTTP 요청 처리 | `*Controller.java` |
| `dto` | 데이터 전송 객체 | `*Dto.java` |
| `entity` | 데이터베이스 엔티티 | 엔티티명 (복수형 지양) |
| `repository` | 데이터 접근 계층 | `*Repository.java` |
| `service` | 비즈니스 로직 | `*Service.java` |

---

## 네이밍 컨벤션

### 클래스 네이밍

#### 1. 엔티티 (Entity)
```java
// ✅ Good - 단수형, 명사
@Table("tests")
public class Test {
    @Id
    private Long id;
}

// ❌ Bad - 복수형 사용
public class Tests { }
```

#### 2. DTO (Data Transfer Object)
```java
// ✅ Good - 엔티티명 + Dto 접미사
public class TestDto {
    private Long id;
    private String name;
}

// ❌ Bad - Request/Response 분리하지 않음
public class TestRequest { }
public class TestResponse { }
```

#### 3. Repository
```java
// ✅ Good - 엔티티명 + Repository
public interface TestRepository extends R2dbcRepository<Test, Long> {
    // 메서드명: 동사 + By + 필드명
    Flux<Test> findByNameContaining(String keyword);
}
```

#### 4. Service
```java
// ✅ Good - 엔티티명 + Service
@Service
public class TestService {
    // 메서드명: 동사 원형
    public Mono<TestDto> create(TestDto testDto) { }
    public Mono<TestDto> findById(Long id) { }
    public Flux<TestDto> findAll() { }
    public Mono<TestDto> update(Long id, TestDto testDto) { }
    public Mono<Void> delete(Long id) { }
}
```

#### 5. Controller
```java
// ✅ Good - 엔티티명 + Controller
@RestController
@RequestMapping("/api/tests")
public class TestController {
    // 메서드명: 동사 + 엔티티명
    public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) { }
    public Mono<ResponseEntity<TestDto>> getTestById(@PathVariable Long id) { }
}
```

#### 6. Client
```java
// ✅ Good - 서비스명 + Client
@Service
public class UserServiceClient {
    private final WebClient webClient;

    public Mono<UserDto> getUserById(Long userId) { }
    public Flux<UserDto> getAllUsers() { }
}
```

#### 7. Config
```java
// ✅ Good - 기능명 + Config
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() { }
}
```

### 변수 네이밍

```java
// ✅ Good - camelCase, 명확한 의미
private final TestRepository testRepository;
private final UserServiceClient userServiceClient;
private LocalDateTime createdAt;

// ❌ Bad - 축약형, 불명확한 의미
private final TestRepository repo;
private final UserServiceClient client;
private LocalDateTime dt;
```

### 상수 네이밍

```java
// ✅ Good - UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;
private static final String DEFAULT_ERROR_MESSAGE = "An error occurred";
```

---

## 코드 스타일

### 1. 클래스 구조 순서

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    // 1. 상수
    private static final int MAX_ITEMS = 100;

    // 2. 필드 (final 필드 우선)
    private final TestRepository testRepository;

    // 3. 생성자 (Lombok의 경우 생략)

    // 4. Public 메서드
    public Mono<TestDto> create(TestDto testDto) { }

    public Mono<TestDto> findById(Long id) { }

    // 5. Private 메서드
    private TestDto convertToDto(Test test) { }
}
```

### 2. 메서드 작성 규칙

#### 한 가지 일만 수행
```java
// ✅ Good - 단일 책임
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

// ❌ Bad - 여러 책임 혼재
public Mono<TestDto> createAndNotify(TestDto testDto) {
    // 생성 + 알림 + 로그 + 검증이 한 메서드에...
}
```

#### 적절한 메서드 길이
- **권장**: 20줄 이내
- **최대**: 50줄
- 길어지면 private 메서드로 분리

### 3. 들여쓰기 및 공백

```java
// ✅ Good
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
```

### 4. 빌더 패턴 스타일

```java
// ✅ Good - 각 속성을 명확하게 구분
Test test = Test.builder()
        .name(testDto.getName())
        .description(testDto.getDescription())
        .createdAt(now)
        .updatedAt(now)
        .build();

// ❌ Bad - 한 줄로 작성
Test test = Test.builder().name(name).description(desc).build();
```

---

## 어노테이션 규칙

### 1. 클래스 레벨 어노테이션 순서

```java
// 순서: Lombok → Spring → 기타
@Data                    // 1. Lombok
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity                  // 2. Spring (JPA)
@Table("tests")
@Schema(description = "Test 데이터 전송 객체")  // 3. 기타 (Swagger)
public class Test { }
```

### 2. Service 클래스 어노테이션

```java
@Slf4j              // 1. Lombok (로깅)
@Service            // 2. Spring 스테레오타입
@RequiredArgsConstructor  // 3. Lombok (생성자 주입)
public class TestService { }
```

### 3. Controller 클래스 어노테이션

```java
@Slf4j                              // 1. Lombok
@RestController                      // 2. Spring
@RequestMapping("/api/tests")        // 3. 매핑 정보
@RequiredArgsConstructor             // 4. Lombok
@Tag(name = "Test Controller", description = "...")  // 5. Swagger
public class TestController { }
```

### 4. 필드 어노테이션

```java
// Entity
@Id
private Long id;

@Column("created_at")
private LocalDateTime createdAt;

// DTO
@Schema(description = "Test ID", example = "1")
private Long id;

@Schema(description = "Test 이름", example = "테스트 이름", required = true)
private String name;
```

### 5. 메서드 어노테이션 (Controller)

```java
@PostMapping
@Operation(summary = "Test 생성", description = "새로운 Test를 생성합니다")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test 생성 성공",
                content = @Content(schema = @Schema(implementation = TestDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
})
public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) {
    // ...
}
```

---

## 반응형 프로그래밍

### 1. 반환 타입 선택

```java
// 단일 결과 → Mono<T>
public Mono<TestDto> findById(Long id) { }

// 복수 결과 → Flux<T>
public Flux<TestDto> findAll() { }

// 결과 없음 → Mono<Void>
public Mono<Void> delete(Long id) { }
```

### 2. Reactive Chain 작성

#### flatMap vs map
```java
// ✅ flatMap - 비동기 작업 체이닝
public Mono<TestDto> update(Long id, TestDto testDto) {
    return testRepository.findById(id)
            .flatMap(test -> {
                test.setName(testDto.getName());
                return testRepository.save(test);
            })
            .map(this::convertToDto);
}

// ✅ map - 동기 변환
private TestDto convertToDto(Test test) { }
```

#### switchIfEmpty
```java
// ✅ Good - 빈 결과 처리
public Mono<TestDto> findById(Long id) {
    return testRepository.findById(id)
            .map(this::convertToDto)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Test not found with ID: " + id)
            ));
}
```

#### doOnSuccess, doOnComplete
```java
// ✅ Good - 사이드 이펙트 (로깅 등)
return testRepository.save(test)
        .doOnSuccess(saved -> log.info("Created Test entity with ID: {}", saved.getId()))
        .map(this::convertToDto);

return testRepository.findAll()
        .doOnComplete(() -> log.info("Retrieved all test entities"))
        .map(this::convertToDto);
```

### 3. WebClient 사용

```java
@Service
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;

    // ✅ Good - WebClient.Builder 주입
    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/users")
                .build();
    }

    // ✅ Good - timeout, error handling
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
}
```

---

## API 설계

### 1. REST 엔드포인트 규칙

```java
@RestController
@RequestMapping("/api/tests")  // 복수형 사용
public class TestController {

    // POST /api/tests - 생성
    @PostMapping
    public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) { }

    // GET /api/tests/{id} - 단건 조회
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TestDto>> getTestById(@PathVariable Long id) { }

    // GET /api/tests - 전체 조회
    @GetMapping
    public Flux<TestDto> getAllTests() { }

    // GET /api/tests/search?keyword=xxx - 검색
    @GetMapping("/search")
    public Flux<TestDto> searchTests(@RequestParam String keyword) { }

    // PUT /api/tests/{id} - 수정
    @PutMapping("/{id}")
    public Mono<ResponseEntity<TestDto>> updateTest(@PathVariable Long id, @RequestBody TestDto testDto) { }

    // DELETE /api/tests/{id} - 삭제
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTest(@PathVariable Long id) { }
}
```

### 2. HTTP 상태 코드 규칙

```java
// 201 Created - 생성 성공
return testService.create(testDto)
        .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));

// 200 OK - 조회/수정 성공
return testService.findById(id)
        .map(ResponseEntity::ok);

// 204 No Content - 삭제 성공
return testService.delete(id)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()));

// 404 Not Found - 리소스 없음
.onErrorResume(e -> {
    log.error("Error fetching test: {}", e.getMessage());
    return Mono.just(ResponseEntity.notFound().build());
});

// 503 Service Unavailable - 외부 서비스 오류
.onErrorResume(e -> {
    log.error("Error fetching test or user: {}", e.getMessage());
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
});
```

### 3. Swagger 문서화

```java
@Operation(summary = "Test 생성", description = "새로운 Test를 생성합니다")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test 생성 성공",
                content = @Content(schema = @Schema(implementation = TestDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
})
public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) {
    // ...
}
```

#### PathVariable, RequestParam 문서화

```java
public Mono<ResponseEntity<TestDto>> getTestById(
        @Parameter(description = "조회할 Test의 ID", required = true)
        @PathVariable Long id) {
    // ...
}

public Flux<TestDto> searchTests(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam String keyword) {
    // ...
}
```

---

## 에러 핸들링

### 1. Service Layer

```java
// ✅ Good - switchIfEmpty로 명확한 에러 처리
public Mono<TestDto> findById(Long id) {
    return testRepository.findById(id)
            .map(this::convertToDto)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Test not found with ID: " + id)
            ));
}

// ✅ Good - existsById로 검증 후 처리
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
```

### 2. Controller Layer

```java
// ✅ Good - onErrorResume으로 에러 변환
@GetMapping("/{id}")
public Mono<ResponseEntity<TestDto>> getTestById(@PathVariable Long id) {
    return testService.findById(id)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Error fetching test: {}", e.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
}
```

### 3. WebClient 에러 핸들링

```java
// ✅ Good - timeout + onErrorResume
public Mono<UserDto> getUserById(Long userId) {
    return webClient.get()
            .uri("/{id}", userId)
            .retrieve()
            .bodyToMono(UserDto.class)
            .timeout(Duration.ofSeconds(3))
            .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getName()))
            .onErrorResume(e -> {
                log.error("Error fetching user {}: {}", userId, e.getMessage());
                return Mono.empty();  // Fallback: 빈 결과 반환
            });
}
```

---

## 로깅

### 1. 로깅 레벨 사용

```java
@Slf4j
@Service
public class TestService {

    // INFO - 주요 비즈니스 로직 완료
    log.info("Created Test entity with ID: {}", saved.getId());
    log.info("Retrieved all test entities");

    // DEBUG - 상세 디버깅 정보 (application.yml에서 설정)
    // logging.level.com.interplug.testservice: DEBUG

    // ERROR - 에러 발생 시
    log.error("Error fetching test: {}", e.getMessage());
    log.error("Error updating test: {}", e.getMessage());
}
```

### 2. 로깅 위치

```java
// ✅ Good - doOnSuccess, doOnComplete에서 로깅
return testRepository.save(test)
        .doOnSuccess(saved -> log.info("Created Test entity with ID: {}", saved.getId()))
        .map(this::convertToDto);

return testRepository.findAll()
        .doOnComplete(() -> log.info("Retrieved all test entities"))
        .map(this::convertToDto);

// ✅ Good - onErrorResume에서 에러 로깅
.onErrorResume(e -> {
    log.error("Error fetching test: {}", e.getMessage());
    return Mono.just(ResponseEntity.notFound().build());
});
```

### 3. 로깅 포맷

```java
// ✅ Good - 파라미터 바인딩 사용
log.info("Created Test entity with ID: {}", saved.getId());
log.info("Fetching Test with ID: {}", id);

// ❌ Bad - 문자열 연결
log.info("Created Test entity with ID: " + saved.getId());
```

---

## 의존성 주입

### 1. 생성자 주입 (Constructor Injection)

```java
// ✅ Good - @RequiredArgsConstructor 사용
@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
}

// ✅ Good - 명시적 생성자 (여러 의존성)
@RestController
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    private final UserServiceClient userServiceClient;
}
```

### 2. WebClient Builder 주입

```java
// ✅ Good - WebClient.Builder 주입 받아서 build()
@Service
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/users")
                .build();
    }
}
```

### 3. Configuration Bean

```java
// ✅ Good - @Bean으로 명시적 정의
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Test Service API (Reactive)")
                        .version("v2.0.0"));
    }
}
```

---

## 추가 컨벤션

### 1. Entity와 DTO 변환

```java
// ✅ Good - private 메서드로 변환 로직 분리
private TestDto convertToDto(Test test) {
    return TestDto.builder()
            .id(test.getId())
            .name(test.getName())
            .description(test.getDescription())
            .createdAt(test.getCreatedAt())
            .updatedAt(test.getUpdatedAt())
            .build();
}

// Service에서 map으로 활용
return testRepository.findById(id)
        .map(this::convertToDto);
```

### 2. 날짜/시간 처리

```java
// ✅ Good - LocalDateTime 사용
LocalDateTime now = LocalDateTime.now();
test.setCreatedAt(now);
test.setUpdatedAt(now);

// Entity 필드
@Column("created_at")
private LocalDateTime createdAt;

@Column("updated_at")
private LocalDateTime updatedAt;
```

### 3. R2DBC Repository 커스텀 메서드

```java
@Repository
public interface TestRepository extends R2dbcRepository<Test, Long> {

    // ✅ Good - 메서드명으로 쿼리 생성
    Flux<Test> findByNameContaining(String keyword);

    // 기타 예시
    Flux<Test> findByCreatedAtAfter(LocalDateTime date);
    Mono<Test> findByName(String name);
}
```

---

## 설정 파일 컨벤션 (application.yml)

```yaml
# 1. Server 설정
server:
  port: 0  # Random port for multiple instances

# 2. Eureka 설정
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka

# 3. Spring 설정
spring:
  application:
    name: test-service  # 서비스 이름 (kebab-case)

  # R2DBC 설정
  r2dbc:
    url: r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:

# 4. 로깅 설정
logging:
  level:
    com.interplug.testservice: DEBUG
    org.springframework.r2dbc: DEBUG

# 5. Swagger 설정
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  api-docs:
    path: /v3/api-docs
    enabled: true
```

---

## 체크리스트

### 코드 작성 전
- [ ] 적절한 패키지에 클래스 생성
- [ ] 클래스명, 메서드명이 컨벤션에 맞는지 확인
- [ ] Lombok 어노테이션 순서 확인

### 코드 작성 중
- [ ] 메서드는 한 가지 일만 수행하는가?
- [ ] 메서드 길이가 적절한가? (20줄 이내 권장)
- [ ] Reactive Chain이 명확한가?
- [ ] 에러 핸들링이 적절한가?

### 코드 작성 후
- [ ] Swagger 문서화 완료
- [ ] 로깅 추가 완료
- [ ] 테스트 코드 작성 완료
- [ ] 빌드 및 실행 확인

---

## 참고 자료

- [Spring WebFlux 공식 문서](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor 공식 문서](https://projectreactor.io/docs)
- [Spring Data R2DBC 공식 문서](https://spring.io/projects/spring-data-r2dbc)
- [Lombok 공식 문서](https://projectlombok.org/)
- [SpringDoc OpenAPI 공식 문서](https://springdoc.org/)

---

## 버전 정보

- Java: 21
- Spring Boot: 3.5.5
- Spring Cloud: 2025.0.0
- Build Tool: Gradle
- 작성일: 2025-10-10
