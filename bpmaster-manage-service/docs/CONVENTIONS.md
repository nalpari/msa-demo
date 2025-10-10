# 코딩 컨벤션 가이드 (Kotlin)

## 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [패키지 구조](#패키지-구조)
3. [네이밍 컨벤션](#네이밍-컨벤션)
4. [Kotlin 코드 스타일](#kotlin-코드-스타일)
5. [어노테이션 규칙](#어노테이션-규칙)
6. [반응형 프로그래밍](#반응형-프로그래밍)
7. [API 설계](#api-설계)
8. [에러 핸들링](#에러-핸들링)
9. [로깅](#로깅)
10. [의존성 주입](#의존성-주입)
11. [트랜잭션 관리](#트랜잭션-관리)

---

## 프로젝트 구조

### 기본 구조
```
bpmaster-manage-service/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com.interplug.bpmastermanageservice/
│   │   │       ├── config/              # 설정 클래스
│   │   │       ├── controller/          # REST 컨트롤러
│   │   │       ├── entity/              # 엔티티 클래스
│   │   │       ├── repository/          # 리포지토리 인터페이스
│   │   │       ├── service/             # 비즈니스 로직
│   │   │       └── BpmasterManageServiceApplication.kt
│   │   └── resources/
│   │       ├── application.yml          # 설정 파일
│   │       └── create_table.sql        # DB 스키마
│   └── test/
│       └── kotlin/
└── build.gradle
```

---

## 패키지 구조

### 계층별 역할 정의

| 패키지 | 역할 | 명명 규칙 |
|--------|------|-----------|
| `config` | 설정 및 Bean 정의 | `*Config.kt` |
| `controller` | HTTP 요청 처리 | `*Controller.kt` |
| `entity` | 데이터베이스 엔티티 | 엔티티명 (복수형 지양) |
| `repository` | 데이터 접근 계층 | `*Repository.kt` |
| `service` | 비즈니스 로직 | `*Service.kt` |

---

## 네이밍 컨벤션

### 클래스 네이밍

#### 1. 엔티티 (Entity)
```kotlin
// ✅ Good - data class 사용, 단수형, 명사
@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,

    @Column("bp_name")
    val bpName: String
)

// ❌ Bad - 복수형 사용
data class BpMasters { }
```

#### 2. Repository
```kotlin
// ✅ Good - 엔티티명 + Repository, interface로 선언
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {
    // 메서드명: 동사 + By + 필드명
    fun findByBpCode(bpCode: String): Mono<BpMaster>
    fun findByBpType(bpType: String): Flux<BpMaster>
}

// ❌ Bad - class 선언
class BpMasterRepository { }
```

#### 3. Service
```kotlin
// ✅ Good - 엔티티명 + Service, class로 선언
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 메서드명: 동사 원형
    fun findAll(): Flux<BpMaster> { }
    fun findById(id: Long): Mono<BpMaster> { }
    fun create(bpMaster: BpMaster): Mono<BpMaster> { }
    fun update(id: Long, updateRequest: BpMaster): Mono<BpMaster> { }
    fun delete(id: Long): Mono<Void> { }
}
```

#### 4. Controller
```kotlin
// ✅ Good - 엔티티명 + Controller, class로 선언
@RestController
@RequestMapping("/api/v1/bp-master")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 메서드명: 동사 + 엔티티명
    fun getAllBpMasters(): Flux<BpMaster> { }
    fun getBpMasterById(id: Long): Mono<ResponseEntity<BpMaster>> { }
}
```

#### 5. Config
```kotlin
// ✅ Good - 기능명 + Config, class로 선언
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.interplug.bpmastermanageservice.repository"])
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig : AbstractR2dbcConfiguration() {
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
```

### 변수 네이밍

```kotlin
// ✅ Good - camelCase, 명확한 의미, val 우선
private val bpMasterRepository: BpMasterRepository
private val logger = LoggerFactory.getLogger(javaClass)
val createdDate: LocalDateTime? = null

// ❌ Bad - 축약형, 불명확한 의미, 불필요한 var
private var repo: BpMasterRepository
private val log = LoggerFactory.getLogger(javaClass)
var dt: LocalDateTime? = null
```

### 함수 파라미터 네이밍

```kotlin
// ✅ Good - 명확한 파라미터명
fun findByCode(bpCode: String): Mono<BpMaster>
fun updateStatus(id: Long, status: String, updatedBy: String? = null): Mono<BpMaster>

// ❌ Bad - 불명확한 파라미터명
fun findByCode(code: String): Mono<BpMaster>
fun updateStatus(id: Long, s: String, by: String? = null): Mono<BpMaster>
```

### 상수 네이밍

```kotlin
// ✅ Good - UPPER_SNAKE_CASE, companion object 사용
companion object {
    private const val DEFAULT_STATUS = "ACTIVE"
    private const val MAX_RETRY_COUNT = 3
    private const val DEFAULT_ERROR_MESSAGE = "An error occurred"
}

// ❌ Bad - camelCase
companion object {
    private const val defaultStatus = "ACTIVE"
}
```

---

## Kotlin 코드 스타일

### 1. Data Class 사용

```kotlin
// ✅ Good - data class로 엔티티 정의
@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,

    @Column("bp_name")
    val bpName: String,

    @Column("status")
    val status: String? = "ACTIVE",

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_date")
    val updatedDate: LocalDateTime? = null
)
```

### 2. 불변성 우선 (val vs var)

```kotlin
// ✅ Good - val 사용 (불변)
val bpMasterRepository: BpMasterRepository
val logger = LoggerFactory.getLogger(javaClass)
val status: String? = "ACTIVE"

// ❌ Bad - 불필요한 var 사용 (가변)
var bpMasterRepository: BpMasterRepository
var status: String? = "ACTIVE"
```

### 3. Null 안전성

```kotlin
// ✅ Good - Safe call(?.), Elvis 연산자(?:), let 사용
val businessRegNo: String? = null

bpMaster.businessRegNo?.let { regNo ->
    bpMasterRepository.existsByBusinessRegNo(regNo)
} ?: Mono.just(false)

val updatedBy = updateRequest.updatedBy ?: "system"

// ❌ Bad - !! 연산자 남용
val regNo = bpMaster.businessRegNo!!  // Null일 경우 예외 발생
```

### 4. 생성자 주입 (Primary Constructor)

```kotlin
// ✅ Good - Primary Constructor 사용
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}

// ✅ Good - 여러 의존성 주입
@RestController
@RequestMapping("/api/v1/bp-master")
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}
```

### 5. 함수 표현식

```kotlin
// ✅ Good - 단일 표현식 함수
fun findAll(): Flux<BpMaster> = bpMasterRepository.findAll()

fun findById(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))

// ✅ Good - 블록 바디 함수 (복잡한 로직)
fun create(bpMaster: BpMaster): Mono<BpMaster> {
    logger.debug("Creating new business partner: ${bpMaster.bpCode}")

    return bpMasterRepository.existsByBpCode(bpMaster.bpCode)
        .flatMap { exists ->
            if (exists) {
                Mono.error(IllegalArgumentException("Business partner with code ${bpMaster.bpCode} already exists"))
            } else {
                val newBp = bpMaster.copy(
                    createdDate = LocalDateTime.now(),
                    updatedDate = LocalDateTime.now()
                )
                bpMasterRepository.save(newBp)
            }
        }
}
```

### 6. 문자열 템플릿

```kotlin
// ✅ Good - 문자열 템플릿 사용
logger.debug("Finding business partner by id: $id")
logger.info("Successfully created business partner: ${it.bpCode}")

Mono.error(NoSuchElementException("Business partner not found with id: $id"))

// ❌ Bad - 문자열 연결
logger.debug("Finding business partner by id: " + id)
Mono.error(NoSuchElementException("Business partner not found with id: " + id))
```

### 7. when 표현식

```kotlin
// ✅ Good - when 표현식으로 다중 조건 처리
return when {
    status == "ACTIVE" && type != null -> bpMasterService.findActiveByType(type)
    status == "ACTIVE" -> bpMasterService.findAllActive()
    type != null -> bpMasterService.findByType(type)
    primaryPfCode != null -> bpMasterService.findByPrimaryPfCode(primaryPfCode)
    else -> bpMasterService.findAll()
}

// ❌ Bad - 복잡한 if-else 체인
if (status == "ACTIVE" && type != null) {
    return bpMasterService.findActiveByType(type)
} else if (status == "ACTIVE") {
    return bpMasterService.findAllActive()
} else if (type != null) {
    return bpMasterService.findByType(type)
} else {
    return bpMasterService.findAll()
}
```

### 8. 데이터 클래스 copy 메서드

```kotlin
// ✅ Good - copy 메서드로 불변 객체 수정
val updated = existing.copy(
    bpName = updateRequest.bpName,
    bpType = updateRequest.bpType,
    status = updateRequest.status,
    updatedDate = LocalDateTime.now(),
    updatedBy = updateRequest.updatedBy
)

// ❌ Bad - 가변 객체로 직접 수정 (data class에서 불가능)
existing.bpName = updateRequest.bpName
existing.status = updateRequest.status
```

---

## 어노테이션 규칙

### 1. Entity 클래스 어노테이션

```kotlin
// 순서: @Table → data class
@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_date")
    val updatedDate: LocalDateTime? = null
)
```

### 2. Repository 인터페이스 어노테이션

```kotlin
// @Repository만 사용
@Repository
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {
    fun findByBpCode(bpCode: String): Mono<BpMaster>
}
```

### 3. Service 클래스 어노테이션

```kotlin
// @Service만 사용 (생성자 주입이므로 @Autowired 불필요)
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}
```

### 4. Controller 클래스 어노테이션

```kotlin
// 순서: @Tag → @RestController → @RequestMapping → @CrossOrigin
@Tag(name = "BP Master", description = "Business Partner Master Data Management APIs")
@RestController
@RequestMapping("/api/v1/bp-master")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}
```

### 5. Config 클래스 어노테이션

```kotlin
// 순서: @Configuration → @Enable* 계열
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.interplug.bpmastermanageservice.repository"])
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig : AbstractR2dbcConfiguration() {
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
```

### 6. Controller 메서드 어노테이션

```kotlin
@Operation(
    summary = "Get all Business Partners",
    description = "Retrieve all Business Partners with optional filtering by status, type, or primary PF code"
)
@ApiResponses(
    value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved Business Partners"),
        ApiResponse(responseCode = "500", description = "Internal server error")
    ]
)
@GetMapping
fun getAllBpMasters(
    @Parameter(description = "Filter by status (e.g., ACTIVE, INACTIVE)")
    @RequestParam(required = false) status: String?,
    @Parameter(description = "Filter by BP type (e.g., VENDOR, CUSTOMER)")
    @RequestParam(required = false) type: String?
): Flux<BpMaster> {
    // ...
}
```

---

## 반응형 프로그래밍

### 1. 반환 타입 선택

```kotlin
// 단일 결과 → Mono<T>
fun findById(id: Long): Mono<BpMaster>

// 복수 결과 → Flux<T>
fun findAll(): Flux<BpMaster>

// 결과 없음 → Mono<Void>
fun delete(id: Long): Mono<Void>

// Nullable 단일 결과 → Mono<T?>
fun findByCodeOrNull(code: String): Mono<BpMaster?>
```

### 2. Reactive Chain 작성

#### flatMap vs map
```kotlin
// ✅ flatMap - 비동기 작업 체이닝 (Mono/Flux 반환)
fun update(id: Long, updateRequest: BpMaster): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
        .flatMap { existing ->
            val updated = existing.copy(
                bpName = updateRequest.bpName,
                updatedDate = LocalDateTime.now()
            )
            bpMasterRepository.save(updated)
        }

// ✅ map - 동기 변환 (일반 객체 반환)
fun findById(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .map { it.copy(status = it.status?.uppercase()) }
```

#### switchIfEmpty
```kotlin
// ✅ Good - 빈 결과 처리
fun findById(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))

// ✅ Good - 빈 결과에 기본값 제공
fun findByCodeOrDefault(code: String): Mono<BpMaster> =
    bpMasterRepository.findByBpCode(code)
        .switchIfEmpty(Mono.just(BpMaster.createDefault()))
```

#### doOnSuccess, doOnNext, doOnComplete
```kotlin
// ✅ Good - 사이드 이펙트 (로깅, 모니터링 등)
fun create(bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterRepository.save(bpMaster)
        .doOnSuccess { logger.info("Successfully created business partner: ${it.bpCode}") }
        .doOnError { logger.error("Failed to create business partner", it) }

fun findAll(): Flux<BpMaster> =
    bpMasterRepository.findAll()
        .doOnNext { logger.debug("Retrieved business partner: ${it.bpCode}") }
        .doOnComplete { logger.info("Retrieved all business partners") }
```

### 3. R2DBC Repository 커스텀 메서드

```kotlin
@Repository
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {

    // ✅ Good - 메서드명으로 쿼리 생성
    fun findByBpCode(bpCode: String): Mono<BpMaster>
    fun findByBpType(bpType: String): Flux<BpMaster>
    fun findByStatus(status: String): Flux<BpMaster>
    fun findByBpTypeAndStatus(bpType: String, status: String): Flux<BpMaster>
    fun existsByBpCode(bpCode: String): Mono<Boolean>
    fun existsByBusinessRegNo(businessRegNo: String): Mono<Boolean>

    // ✅ Good - @Query 어노테이션으로 커스텀 쿼리
    @Query("SELECT * FROM bp_master WHERE bp_name LIKE CONCAT('%', :bpName, '%')")
    fun searchByBpName(bpName: String): Flux<BpMaster>

    @Query("SELECT * FROM bp_master WHERE status = 'ACTIVE' ORDER BY bp_name")
    fun findAllActive(): Flux<BpMaster>

    @Query("SELECT * FROM bp_master WHERE bp_type = :bpType AND status = 'ACTIVE' ORDER BY bp_name")
    fun findActiveByType(bpType: String): Flux<BpMaster>
}
```

### 4. Reactive 에러 처리 패턴

```kotlin
// ✅ Good - flatMap 내부에서 조건부 에러
fun create(bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterRepository.existsByBpCode(bpMaster.bpCode)
        .flatMap { exists ->
            if (exists) {
                Mono.error(IllegalArgumentException("BP Code already exists: ${bpMaster.bpCode}"))
            } else {
                bpMasterRepository.save(bpMaster)
            }
        }

// ✅ Good - onErrorResume으로 fallback
fun findByIdWithFallback(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .onErrorResume { ex ->
            logger.error("Error finding BP: $id", ex)
            Mono.empty()
        }

// ✅ Good - onErrorMap으로 에러 변환
fun createWithErrorMapping(bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterRepository.save(bpMaster)
        .onErrorMap { ex ->
            when (ex) {
                is IllegalArgumentException -> ResponseStatusException(HttpStatus.CONFLICT, ex.message, ex)
                else -> ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create BP", ex)
            }
        }
```

---

## API 설계

### 1. REST 엔드포인트 규칙

```kotlin
@RestController
@RequestMapping("/api/v1/bp-master")  // API 버전 포함, 복수형 리소스명
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    // GET /api/v1/bp-master - 전체 조회
    @GetMapping
    fun getAllBpMasters(): Flux<BpMaster>

    // GET /api/v1/bp-master/{id} - 단건 조회
    @GetMapping("/{id}")
    fun getBpMasterById(@PathVariable id: Long): Mono<ResponseEntity<BpMaster>>

    // GET /api/v1/bp-master/code/{code} - 코드로 조회
    @GetMapping("/code/{code}")
    fun getBpMasterByCode(@PathVariable code: String): Mono<ResponseEntity<BpMaster>>

    // GET /api/v1/bp-master/search?name=xxx - 검색
    @GetMapping("/search")
    fun searchBpMasters(@RequestParam name: String): Flux<BpMaster>

    // POST /api/v1/bp-master - 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster>

    // PUT /api/v1/bp-master/{id} - 전체 수정
    @PutMapping("/{id}")
    fun updateBpMaster(@PathVariable id: Long, @RequestBody bpMaster: BpMaster): Mono<ResponseEntity<BpMaster>>

    // PATCH /api/v1/bp-master/{id}/status - 부분 수정
    @PatchMapping("/{id}/status")
    fun updateBpMasterStatus(@PathVariable id: Long, @RequestParam status: String): Mono<ResponseEntity<BpMaster>>

    // DELETE /api/v1/bp-master/{id} - 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBpMaster(@PathVariable id: Long): Mono<Void>
}
```

### 2. HTTP 상태 코드 규칙

```kotlin
// 201 Created - 생성 성공
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterService.create(bpMaster)

// 200 OK - 조회/수정 성공
@GetMapping("/{id}")
fun getBpMasterById(@PathVariable id: Long): Mono<ResponseEntity<BpMaster>> =
    bpMasterService.findById(id)
        .map { ResponseEntity.ok(it) }
        .defaultIfEmpty(ResponseEntity.notFound().build())

// 204 No Content - 삭제 성공
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
fun deleteBpMaster(@PathVariable id: Long): Mono<Void> =
    bpMasterService.delete(id)

// 404 Not Found - 리소스 없음
.defaultIfEmpty(ResponseEntity.notFound().build())

// 409 Conflict - 리소스 충돌
.onErrorMap { ex ->
    when (ex) {
        is IllegalArgumentException -> ResponseStatusException(HttpStatus.CONFLICT, ex.message, ex)
        else -> ex
    }
}
```

### 3. Swagger 문서화

```kotlin
@Operation(
    summary = "Create Business Partner",
    description = "Create a new Business Partner with the provided information"
)
@ApiResponses(
    value = [
        ApiResponse(responseCode = "201", description = "Business Partner created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "409", description = "Business Partner already exists"),
        ApiResponse(responseCode = "500", description = "Internal server error")
    ]
)
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
fun createBpMaster(
    @Parameter(description = "Business Partner data to create", required = true)
    @RequestBody bpMaster: BpMaster
): Mono<BpMaster> {
    logger.info("POST /api/v1/bp-master - Creating BP: ${bpMaster.bpCode}")
    return bpMasterService.create(bpMaster)
}
```

#### PathVariable, RequestParam 문서화

```kotlin
@GetMapping("/{id}")
fun getBpMasterById(
    @Parameter(description = "Business Partner ID", required = true)
    @PathVariable id: Long
): Mono<ResponseEntity<BpMaster>> {
    // ...
}

@GetMapping("/search")
fun searchBpMasters(
    @Parameter(description = "Business Partner name to search", required = true, example = "테스트")
    @RequestParam name: String
): Flux<BpMaster> {
    // ...
}

@PatchMapping("/{id}/status")
fun updateBpMasterStatus(
    @Parameter(description = "Business Partner ID", required = true)
    @PathVariable id: Long,
    @Parameter(description = "New status (ACTIVE, INACTIVE, SUSPENDED)", required = true, example = "ACTIVE")
    @RequestParam status: String,
    @Parameter(description = "User who updated the status")
    @RequestParam(required = false) updatedBy: String?
): Mono<ResponseEntity<BpMaster>> {
    // ...
}
```

---

## 에러 핸들링

### 1. Service Layer

```kotlin
// ✅ Good - switchIfEmpty로 명확한 에러 처리
fun findById(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))

// ✅ Good - flatMap 내부에서 조건부 에러
fun create(bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterRepository.existsByBpCode(bpMaster.bpCode)
        .flatMap { exists ->
            if (exists) {
                Mono.error(IllegalArgumentException("Business partner with code ${bpMaster.bpCode} already exists"))
            } else {
                val newBp = bpMaster.copy(
                    createdDate = LocalDateTime.now(),
                    updatedDate = LocalDateTime.now()
                )
                bpMasterRepository.save(newBp)
            }
        }

// ✅ Good - doOnError로 에러 로깅
fun delete(id: Long): Mono<Void> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
        .flatMap { bpMasterRepository.delete(it) }
        .doOnSuccess { logger.info("Successfully deleted business partner id: $id") }
        .doOnError { logger.error("Failed to delete business partner id: $id", it) }
```

### 2. Controller Layer

```kotlin
// ✅ Good - defaultIfEmpty로 404 처리
@GetMapping("/{id}")
fun getBpMasterById(@PathVariable id: Long): Mono<ResponseEntity<BpMaster>> =
    bpMasterService.findById(id)
        .map { ResponseEntity.ok(it) }
        .defaultIfEmpty(ResponseEntity.notFound().build())

// ✅ Good - onErrorMap으로 HTTP 에러 변환
@PostMapping
fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterService.create(bpMaster)
        .onErrorMap { ex ->
            when (ex) {
                is IllegalArgumentException -> ResponseStatusException(HttpStatus.CONFLICT, ex.message, ex)
                else -> ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create BP", ex)
            }
        }
```

---

## 로깅

### 1. 로거 선언

```kotlin
// ✅ Good - javaClass 사용
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}

// ✅ Good - 클래스명 명시
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(BpMasterService::class.java)
}

// ❌ Bad - 문자열로 클래스명 지정
private val logger = LoggerFactory.getLogger("BpMasterService")
```

### 2. 로깅 레벨 사용

```kotlin
// DEBUG - 상세 디버깅 정보
logger.debug("Finding business partner by id: $id")
logger.debug("Checking if business partner code exists: $bpCode")

// INFO - 주요 비즈니스 로직 완료
logger.info("Successfully created business partner: ${it.bpCode}")
logger.info("Successfully updated status for business partner id: $id to $status")

// ERROR - 에러 발생 시
logger.error("Failed to create business partner: ${bpMaster.bpCode}", it)
logger.error("Failed to update business partner id: $id", it)
```

### 3. 로깅 위치

```kotlin
// ✅ Good - doOnSuccess, doOnError에서 로깅
fun create(bpMaster: BpMaster): Mono<BpMaster> =
    bpMasterRepository.save(bpMaster)
        .doOnSuccess { logger.info("Successfully created business partner: ${it.bpCode}") }
        .doOnError { logger.error("Failed to create business partner: ${bpMaster.bpCode}", it) }

// ✅ Good - Controller에서 요청 로깅
@PostMapping
fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster> {
    logger.info("POST /api/v1/bp-master - Creating BP: ${bpMaster.bpCode}")
    return bpMasterService.create(bpMaster)
}
```

### 4. 로깅 포맷

```kotlin
// ✅ Good - 문자열 템플릿 사용
logger.info("Successfully created business partner: ${it.bpCode}")
logger.debug("Finding business partner by id: $id")

// ❌ Bad - 문자열 연결
logger.info("Successfully created business partner: " + it.bpCode)
logger.debug("Finding business partner by id: " + id)
```

---

## 의존성 주입

### 1. 생성자 주입 (Constructor Injection)

```kotlin
// ✅ Good - Primary Constructor 사용
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}

// ✅ Good - 여러 의존성 주입
@RestController
@RequestMapping("/api/v1/bp-master")
class BpMasterController(
    private val bpMasterService: BpMasterService,
    private val bpContractService: BpContractInfoService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
}
```

### 2. Configuration Bean

```kotlin
// ✅ Good - @Bean으로 명시적 정의
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("BPMaster Manage Service API")
                    .description("Business Partner Master Data Management Service API Documentation")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Interplug Development Team")
                            .email("dev@interplug.com")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("/")
                        .description("Current Server (Dynamic Port)"),
                    Server()
                        .url("http://localhost:8000")
                        .description("Via API Gateway")
                )
            )
}
```

---

## 트랜잭션 관리

### 1. Service 트랜잭션

```kotlin
// ✅ Good - @Transactional 사용
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    @Transactional
    fun create(bpMaster: BpMaster): Mono<BpMaster> =
        bpMasterRepository.existsByBpCode(bpMaster.bpCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("BP Code already exists"))
                } else {
                    bpMasterRepository.save(bpMaster)
                }
            }

    @Transactional
    fun update(id: Long, updateRequest: BpMaster): Mono<BpMaster> =
        bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("BP not found")))
            .flatMap { existing ->
                val updated = existing.copy(
                    bpName = updateRequest.bpName,
                    updatedDate = LocalDateTime.now()
                )
                bpMasterRepository.save(updated)
            }
}
```

### 2. Config에서 트랜잭션 매니저 설정

```kotlin
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.interplug.bpmastermanageservice.repository"])
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory {
        throw UnsupportedOperationException("ConnectionFactory is provided by Spring Boot auto-configuration")
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)
}
```

---

## 추가 컨벤션

### 1. 날짜/시간 처리

```kotlin
// ✅ Good - LocalDateTime 사용
@Column("created_date")
val createdDate: LocalDateTime? = null

@Column("updated_date")
val updatedDate: LocalDateTime? = null

// ✅ Good - LocalDate 사용 (날짜만 필요한 경우)
@Column("contract_start_date")
val contractStartDate: LocalDate

@Column("contract_end_date")
val contractEndDate: LocalDate? = null

// Service에서 현재 시간 설정
val newBp = bpMaster.copy(
    createdDate = LocalDateTime.now(),
    updatedDate = LocalDateTime.now()
)
```

### 2. BigDecimal 사용 (금액/비율)

```kotlin
// ✅ Good - BigDecimal 사용
@Column("erp_usage_fee")
val erpUsageFee: BigDecimal? = null

@Column("commission_rate")
val commissionRate: BigDecimal? = null

@Column("fee_rate")
val feeRate: BigDecimal? = null
```

### 3. 기본값 설정

```kotlin
// ✅ Good - 파라미터 기본값 설정
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("status")
    val status: String? = "ACTIVE",

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null
)

// ✅ Good - 함수 파라미터 기본값
fun updateStatus(id: Long, status: String, updatedBy: String? = null): Mono<BpMaster>
```

### 4. 컬렉션 처리

```kotlin
// ✅ Good - listOf(), emptyList() 사용
val servers = listOf(
    Server().url("/").description("Current Server"),
    Server().url("http://localhost:8000").description("Via API Gateway")
)

// ✅ Good - filter, map 등 고차 함수 사용
val activeBps = bpList.filter { it.status == "ACTIVE" }
val bpCodes = bpList.map { it.bpCode }
```

---

## 설정 파일 컨벤션 (application.yml)

```yaml
# 1. Spring 기본 설정
spring:
  application:
    name: bpmaster-manage-service  # 서비스 이름 (kebab-case)

  # 2. R2DBC PostgreSQL 설정
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/whaleerpdb
    username: whaleerp
    password: whaleerp12345!@
    pool:
      enabled: true
      initial-size: 5
      max-size: 20

  # 3. Data 설정
  data:
    r2dbc:
      repositories:
        enabled: true

# 4. Eureka 설정
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

# 5. 서버 설정
server:
  port: 0  # Random port

# 6. 로깅 설정
logging:
  level:
    root: INFO
    com.interplug.bpmastermanageservice: DEBUG
    org.springframework.r2dbc: DEBUG

# 7. Actuator 설정
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers

# 8. Resilience4j 설정
resilience4j:
  circuitbreaker:
    instances:
      bpmaster-service:
        sliding-window-size: 10
        failure-rate-threshold: 50

# 9. SpringDoc 설정
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

---

## 체크리스트

### 코드 작성 전
- [ ] 적절한 패키지에 클래스 생성
- [ ] 클래스명, 함수명이 컨벤션에 맞는지 확인
- [ ] data class 사용 여부 확인 (Entity)

### 코드 작성 중
- [ ] val 사용 (불변성 우선)
- [ ] Null 안전성 확인 (Safe call, Elvis 연산자)
- [ ] Reactive Chain이 명확한가?
- [ ] 에러 핸들링이 적절한가?

### 코드 작성 후
- [ ] Swagger 문서화 완료
- [ ] 로깅 추가 완료
- [ ] 트랜잭션 처리 확인
- [ ] 테스트 코드 작성 완료
- [ ] 빌드 및 실행 확인

---

## 참고 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin 코드 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)
- [Spring WebFlux 공식 문서](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Spring Data R2DBC 공식 문서](https://spring.io/projects/spring-data-r2dbc)
- [Reactor Kotlin Extensions](https://github.com/reactor/reactor-kotlin-extensions)
- [SpringDoc OpenAPI 공식 문서](https://springdoc.org/)

---

## 버전 정보

- Kotlin: 1.9.25
- Java: 17
- Spring Boot: 3.5.5
- Spring Cloud: 2025.0.0
- Build Tool: Gradle
- 작성일: 2025-10-10
