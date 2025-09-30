# BPMaster Manage Service

이 파일은 Claude Code가 BPMaster Manage Service 모듈에서 작업할 때 가이드를 제공합니다.

## 서비스 개요

BPMaster Manage Service는 비즈니스 파트너(BP) 및 플랫폼(PF) 관련 마스터 데이터를 관리하는 반응형 마이크로서비스입니다. WhaleERP 시스템의 핵심 구성 요소로, 비즈니스 파트너 정보, 계약 관리, 매장 정보, 권한 관리 등을 담당합니다.

### 주요 특징
- **Kotlin 기반** 반응형 프로그래밍 모델
- **Spring WebFlux** 비동기 논블로킹 웹 프레임워크
- **R2DBC PostgreSQL** 반응형 데이터베이스 드라이버
- **Resilience4j Circuit Breaker** 장애 격리 및 복구
- **SpringDoc OpenAPI** Swagger UI 지원 (WebFlux 최적화)
- **Eureka Client** 서비스 디스커버리
- **Spring Cloud Config** 선택적 중앙화 설정 (현재 비활성화)

## 기술 스택

### 핵심 기술
- **언어**: Kotlin 1.9.25
- **프레임워크**: Spring Boot 3.5.5
- **Spring Cloud**: 2025.0.0
- **반응형 스택**: Spring WebFlux, Reactor Core
- **데이터베이스**: PostgreSQL 15+ with R2DBC
- **빌드 도구**: Gradle
- **Java 버전**: 21 (Gradle 설정 기준)

### 주요 의존성
```kotlin
// 핵심 의존성
- spring-boot-starter-webflux       // 반응형 웹
- spring-boot-starter-data-r2dbc    // 반응형 DB
- reactor-kotlin-extensions         // Kotlin 반응형 확장
- kotlinx-coroutines-reactor       // 코루틴-리액터 통합

// Spring Cloud
- spring-cloud-starter-netflix-eureka-client  // 서비스 디스커버리
- spring-cloud-starter-circuitbreaker-reactor-resilience4j  // 회로 차단기
// spring-cloud-starter-config       // 설정 서버 (현재 비활성화)

// 데이터베이스 드라이버
- postgresql                        // JDBC 드라이버
- r2dbc-postgresql                  // R2DBC 드라이버

// API 문서화
- springdoc-openapi-starter-webflux-ui:2.7.0  // Swagger UI
```

## 프로젝트 구조

```
bpmaster-manage-service/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/interplug/bpmastermanageservice/
│   │   │       ├── BpmasterManageServiceApplication.kt
│   │   │       ├── config/
│   │   │       │   └── R2dbcConfig.kt        # R2DBC 설정
│   │   │       ├── controller/
│   │   │       │   ├── BpMasterController.kt           # BP 마스터 API
│   │   │       │   ├── BpContractInfoController.kt     # 계약 정보 API
│   │   │       │   ├── BpStoreInfoController.kt        # 매장 정보 API
│   │   │       │   ├── BpPfMappingController.kt        # BP-PF 매핑 API
│   │   │       │   ├── BpMasterDataPermissionController.kt  # 권한 관리 API
│   │   │       │   └── PfCodeMasterController.kt       # PF 코드 마스터 API
│   │   │       ├── service/
│   │   │       │   ├── BpMasterService.kt
│   │   │       │   ├── BpContractInfoService.kt
│   │   │       │   ├── BpStoreInfoService.kt
│   │   │       │   ├── BpPfMappingService.kt
│   │   │       │   ├── BpMasterDataPermissionService.kt
│   │   │       │   └── PfCodeMasterService.kt
│   │   │       ├── repository/
│   │   │       │   ├── BpMasterRepository.kt
│   │   │       │   ├── BpContractInfoRepository.kt
│   │   │       │   ├── BpStoreInfoRepository.kt
│   │   │       │   ├── BpPfMappingRepository.kt
│   │   │       │   ├── BpMasterDataPermissionRepository.kt
│   │   │       │   └── PfCodeMasterRepository.kt
│   │   │       └── entity/
│   │   │           ├── BpMaster.kt               # BP 마스터 엔티티
│   │   │           ├── BpContractInfo.kt         # 계약 정보 엔티티
│   │   │           ├── BpStoreInfo.kt            # 매장 정보 엔티티
│   │   │           ├── BpPfMapping.kt            # BP-PF 매핑 엔티티
│   │   │           ├── BpMasterDataPermission.kt # 권한 관리 엔티티
│   │   │           └── PfCodeMaster.kt           # PF 코드 마스터 엔티티
│   │   └── resources/
│   │       ├── application.yml       # 애플리케이션 설정
│   │       └── create_table.sql      # 테이블 생성 스크립트
│   └── test/
│       └── kotlin/
├── build.gradle
├── CLAUDE.md           # 이 문서
└── SWAGGER-SETUP.md    # Swagger 설정 가이드
```

## 데이터베이스 설정

### PostgreSQL R2DBC 연결 설정
```yaml
# application.yml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/whaleerpdb
    username: whaleerp
    password: whaleerp12345!@
    pool:
      enabled: true
      initial-size: 5
      max-size: 20
      max-idle-time: 30m
      max-acquire-time: 5s
      max-create-connection-time: 5s
      validation-query: SELECT 1
```

### 주요 엔티티

#### BpMaster (비즈니스 파트너 마스터)
```kotlin
@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,                    // BP 코드 (고유값)

    @Column("bp_name")
    val bpName: String,                    // BP 명칭

    @Column("bp_type")
    val bpType: String,                    // BP 유형 (VENDOR, CUSTOMER 등)

    @Column("business_reg_no")
    val businessRegNo: String? = null,     // 사업자등록번호

    @Column("representative_name")
    val representativeName: String? = null, // 대표자명

    @Column("primary_pf_code")
    val primaryPfCode: String? = null,     // 주 플랫폼 코드

    @Column("status")
    val status: String? = "ACTIVE",        // 상태 (ACTIVE, INACTIVE 등)

    @Column("erp_usage_fee")
    val erpUsageFee: BigDecimal? = null,   // ERP 사용료

    @Column("commission_rate")
    val commissionRate: BigDecimal? = null, // 수수료율

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_date")
    val updatedDate: LocalDateTime? = null
)
```

## API 엔드포인트

### Base URL
- 개발: `http://localhost:{dynamic-port}/api/v1`
- Eureka를 통한 접근: `http://bpmaster-manage-service/api/v1`

### 주요 API

#### BpMaster APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/bp-master` | 전체 BP 조회 (status, type, primaryPfCode 필터링 가능) |
| GET | `/bp-master/{id}` | ID로 BP 조회 |
| GET | `/bp-master/code/{code}` | 코드로 BP 조회 |
| GET | `/bp-master/search?name={name}` | 이름으로 BP 검색 |
| POST | `/bp-master` | BP 생성 |
| PUT | `/bp-master/{id}` | BP 수정 |
| PATCH | `/bp-master/{id}/status` | BP 상태 변경 |
| DELETE | `/bp-master/{id}` | BP 삭제 |
| GET | `/bp-master/check/business-reg-no/{businessRegNo}` | 사업자등록번호 중복 체크 |
| GET | `/bp-master/check/code/{code}` | BP 코드 중복 체크 |

#### 기타 도메인 APIs
- **BpContractInfo**: `/bp-contract` - 계약 정보 관리
- **BpStoreInfo**: `/bp-store` - 매장 정보 관리
- **BpPfMapping**: `/bp-pf-mapping` - BP-PF 매핑 관리
- **BpMasterDataPermission**: `/bp-permission` - 권한 관리
- **PfCodeMaster**: `/pf-code` - PF 코드 마스터 관리

### Swagger UI
- URL: `http://localhost:{port}/swagger-ui.html`
- API 문서: `http://localhost:{port}/v3/api-docs`

## 반응형 프로그래밍 패턴

### R2DBC Repository 예시
```kotlin
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {

    fun findByBpCode(bpCode: String): Mono<BpMaster>

    fun findByBpType(bpType: String): Flux<BpMaster>

    fun findByStatus(status: String): Flux<BpMaster>

    fun findByStatusAndBpType(status: String, bpType: String): Flux<BpMaster>

    fun findByPrimaryPfCode(primaryPfCode: String): Flux<BpMaster>

    fun existsByBpCode(bpCode: String): Mono<Boolean>

    fun existsByBusinessRegNo(businessRegNo: String): Mono<Boolean>

    @Query("SELECT * FROM bp_master WHERE LOWER(bp_name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(name: String): Flux<BpMaster>
}
```

### Reactive Service 패턴
```kotlin
@Service
class BpMasterService(
    private val repository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findById(id: Long): Mono<BpMaster> =
        repository.findById(id)
            .doOnSubscribe { logger.debug("Finding BP by ID: $id") }
            .doOnNext { logger.debug("Found BP: ${it.bpCode}") }

    fun create(bpMaster: BpMaster): Mono<BpMaster> =
        repository.existsByBpCode(bpMaster.bpCode)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalArgumentException("BP Code already exists: ${bpMaster.bpCode}"))
                } else {
                    repository.save(bpMaster)
                        .doOnSuccess { logger.info("Created BP: ${it.bpCode}") }
                }
            }

    fun updateStatus(id: Long, status: String, updatedBy: String?): Mono<BpMaster> =
        repository.findById(id)
            .flatMap { existing ->
                val updated = existing.copy(
                    status = status,
                    updatedDate = LocalDateTime.now(),
                    updatedBy = updatedBy ?: "system"
                )
                repository.save(updated)
            }
            .doOnSuccess { logger.info("Updated BP status: ${it.bpCode} to $status") }
}
```

### WebFlux Controller 패턴
```kotlin
@RestController
@RequestMapping("/api/v1/bp-master")
class BpMasterController(
    private val service: BpMasterService
) {
    @GetMapping("/{id}")
    fun getBpMasterById(@PathVariable id: Long): Mono<ResponseEntity<BpMaster>> =
        service.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster> =
        service.create(bpMaster)
            .onErrorMap { ex ->
                when (ex) {
                    is IllegalArgumentException -> ResponseStatusException(HttpStatus.CONFLICT, ex.message, ex)
                    else -> ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create BP", ex)
                }
            }
}
```

## Circuit Breaker 설정

### Resilience4j 설정 (application.yml)
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10              # 슬라이딩 윈도우 크기
        minimum-number-of-calls: 5           # 최소 호출 수
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s     # Open 상태 대기 시간
        failure-rate-threshold: 50           # 실패율 임계값 (%)
        slow-call-rate-threshold: 50         # 느린 호출 비율 임계값 (%)
        slow-call-duration-threshold: 2s     # 느린 호출 기준 시간
        automatic-transition-from-open-to-half-open-enabled: true
    instances:
      bpmaster-service:
        base-config: default
```

### Service에서 Circuit Breaker 사용
```kotlin
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.timelimiter.annotation.TimeLimiter

@Service
class ExternalApiService {

    @CircuitBreaker(name = "bpmaster-service", fallbackMethod = "fallbackGetData")
    @TimeLimiter(name = "bpmaster-service")
    fun getExternalData(id: Long): Mono<Data> =
        webClient.get()
            .uri("/api/data/{id}", id)
            .retrieve()
            .bodyToMono(Data::class.java)

    fun fallbackGetData(id: Long, exception: Exception): Mono<Data> {
        logger.warn("Circuit breaker activated for id: $id", exception)
        return Mono.just(Data.default())
    }
}
```

## 코루틴 통합

### 코루틴 사용 예시
```kotlin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull

// Suspend 함수로 변환
suspend fun getBpMasterSuspend(id: Long): BpMaster? =
    repository.findById(id).awaitFirstOrNull()

// Flow로 변환
fun getBpMastersFlow(): Flow<BpMaster> =
    repository.findAll().asFlow()

// 코루틴 기반 Controller
@RestController
@RequestMapping("/api/v1/bp-master-coroutine")
class BpMasterCoroutineController(
    private val service: BpMasterService
) {
    @GetMapping("/{id}")
    suspend fun getBpMaster(@PathVariable id: Long): ResponseEntity<BpMaster> {
        val bp = service.findById(id).awaitFirstOrNull()
        return if (bp != null) {
            ResponseEntity.ok(bp)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
```

## 실행 및 테스트

### 사전 요구사항
```bash
# PostgreSQL 실행 (이미 실행 중인 경우 생략)
# DB: whaleerpdb, User: whaleerp, Password: whaleerp12345!@
docker run -d \
  --name whaleerpdb \
  -e POSTGRES_USER=whaleerp \
  -e POSTGRES_PASSWORD=whaleerp12345!@ \
  -e POSTGRES_DB=whaleerpdb \
  -p 5432:5432 \
  postgres:15

# Eureka Server 실행 (선택적)
cd ../eureka && ./gradlew bootRun
```

### 서비스 실행
```bash
# 개발 모드 실행
cd bpmaster-manage-service
./gradlew bootRun

# JAR 빌드 및 실행
./gradlew clean build
java -jar build/libs/bpmaster-manage-service-0.0.1-SNAPSHOT.jar
```

### 테스트
```bash
# 단위 테스트
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests BpMasterServiceTests
```

### API 테스트
```bash
# 전체 BP 조회
curl http://localhost:{port}/api/v1/bp-master

# BP 코드로 조회
curl http://localhost:{port}/api/v1/bp-master/code/BP001

# BP 생성
curl -X POST http://localhost:{port}/api/v1/bp-master \
  -H "Content-Type: application/json" \
  -d '{
    "bpCode": "BP001",
    "bpName": "테스트 파트너",
    "bpType": "VENDOR",
    "businessRegNo": "123-45-67890",
    "representativeName": "홍길동",
    "primaryPfCode": "PF001"
  }'

# BP 상태 변경
curl -X PATCH "http://localhost:{port}/api/v1/bp-master/1/status?status=INACTIVE&updatedBy=admin"

# 사업자등록번호 중복 체크
curl http://localhost:{port}/api/v1/bp-master/check/business-reg-no/123-45-67890
```

## 모니터링 및 관찰

### Actuator 엔드포인트
- Health: `http://localhost:{port}/actuator/health`
- Metrics: `http://localhost:{port}/actuator/metrics`
- Circuit Breakers: `http://localhost:{port}/actuator/circuitbreakers`
- Prometheus: `http://localhost:{port}/actuator/prometheus`

### 로그 확인
```bash
# 실시간 로그 확인
tail -f logs/bpmaster-manage-service.log

# 특정 레벨 로그 필터링
grep "ERROR" logs/bpmaster-manage-service.log
grep "DEBUG.*BpMasterService" logs/bpmaster-manage-service.log
```

## 개발 가이드라인

### 반응형 프로그래밍 원칙
1. **논블로킹**: 모든 I/O 작업은 비동기로 처리
2. **배압(Backpressure)**: 데이터 스트림 처리 시 소비자 속도 고려
3. **에러 처리**: `onErrorResume`, `onErrorReturn`, `onErrorMap` 활용
4. **리소스 관리**: `using`, `defer` 연산자로 리소스 정리
5. **Cold vs Hot Streams**: 구독 시점 이해하고 적절히 활용

### Kotlin 코드 스타일
- **불변성 우선**: `val` 사용, `var`는 필요한 경우만
- **Null 안전성**: Elvis 연산자(`?:`), Safe call(`?.`) 활용
- **Data Classes**: 엔티티, DTO에 활용
- **Extension Functions**: 코드 재사용성 향상
- **Coroutines**: 필요시 Reactor와 통합 사용

### 성능 최적화
```kotlin
// Connection Pool 설정 예시
spring:
  r2dbc:
    pool:
      initial-size: 5      # 초기 연결 수
      max-size: 20         # 최대 연결 수
      max-idle-time: 30m   # 유휴 연결 제거 시간
      max-acquire-time: 5s # 연결 획득 대기 시간

// 대량 데이터 처리
fun processLargeData(): Flux<Result> =
    repository.findAll()
        .buffer(100)        // 100개씩 배치 처리
        .flatMap { batch ->
            processBatch(batch)
        }
        .onBackpressureBuffer(1000)  // 배압 처리
```

## 트러블슈팅

### 일반적인 이슈

#### 1. R2DBC 연결 실패
```bash
# 문제 확인
psql -h localhost -U whaleerp -d whaleerpdb

# 해결책
- PostgreSQL 서비스 실행 확인
- 방화벽/포트 확인 (5432)
- pg_hba.conf 인증 설정 확인
```

#### 2. 반응형 스트림 차단
```kotlin
// 잘못된 예
fun blockingOperation() {
    val result = repository.findById(1).block()  // WebFlux에서 block() 사용 금지
}

// 올바른 예
fun nonBlockingOperation(): Mono<Result> =
    repository.findById(1)
        .map { processResult(it) }
```

#### 3. Circuit Breaker 문제
```yaml
# 로그 확인
logging:
  level:
    io.github.resilience4j: DEBUG

# Actuator로 상태 확인
curl http://localhost:{port}/actuator/circuitbreakers
```

#### 4. Eureka 등록 실패
```bash
# 해결책
- Eureka Server 실행 상태 확인
- defaultZone URL 확인
- 네트워크 연결 확인
- 로그에서 등록 시도 확인
```

## 추가 기능 구현 예시

### 페이징 처리
```kotlin
fun findAllWithPaging(page: Int, size: Int): Flux<BpMaster> =
    repository.findAll()
        .skip((page * size).toLong())
        .take(size.toLong())

// 또는 Spring Data R2DBC Pageable 사용
fun findByType(type: String, pageable: Pageable): Flux<BpMaster> =
    repository.findByBpType(type, pageable)
```

### 트랜잭션 처리
```kotlin
@Transactional
fun createWithRelations(bpMaster: BpMaster, stores: List<BpStoreInfo>): Mono<BpMaster> =
    bpMasterRepository.save(bpMaster)
        .flatMap { saved ->
            val storeMonos = stores.map { store ->
                storeRepository.save(store.copy(bpId = saved.bpId))
            }
            Flux.merge(storeMonos)
                .then(Mono.just(saved))
        }
```

## 참고 자료

### 공식 문서
- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/3.5.5/reference/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)
- [SpringDoc OpenAPI](https://springdoc.org/)

### 프로젝트 관련 문서
- [SWAGGER-SETUP.md](./SWAGGER-SETUP.md) - Swagger UI 설정 가이드
- [메인 프로젝트 CLAUDE.md](../CLAUDE.md) - MSA 전체 구조