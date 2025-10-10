# BPMaster Manage Service

Kotlin 기반 Spring WebFlux 반응형 마이크로서비스 - 비즈니스 파트너 마스터 데이터 관리

## 목차
- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 도메인](#주요-도메인)
- [API 엔드포인트](#api-엔드포인트)
- [데이터베이스](#데이터베이스)
- [설정](#설정)
- [실행 방법](#실행-방법)
- [API 테스트](#api-테스트)
- [Swagger UI](#swagger-ui)
- [반응형 프로그래밍](#반응형-프로그래밍)
- [Circuit Breaker](#circuit-breaker)
- [트러블슈팅](#트러블슈팅)

---

## 개요

bpmaster-manage-service는 WhaleERP 시스템의 핵심 마이크로서비스로, 비즈니스 파트너(BP) 및 플랫폼(PF) 관련 마스터 데이터를 관리합니다. Kotlin과 Spring WebFlux를 사용하여 높은 처리량과 낮은 지연 시간을 제공하는 반응형 아키텍처로 구현되었습니다.

### 주요 특징
- **Kotlin**: 간결하고 안전한 코드 작성
- **Spring WebFlux**: 비동기 논블로킹 웹 프레임워크
- **R2DBC PostgreSQL**: 반응형 데이터베이스 드라이버
- **Resilience4j**: Circuit Breaker를 통한 장애 격리
- **Eureka Client**: 서비스 디스커버리
- **Swagger UI**: API 문서화 (WebFlux 최적화)

### 관리 도메인
- **BP Master**: 비즈니스 파트너 기본 정보
- **BP Contract Info**: 계약 정보
- **BP Store Info**: 매장 정보
- **BP-PF Mapping**: BP-PF 매핑 관계
- **BP Master Data Permission**: 데이터 권한 관리
- **PF Code Master**: 플랫폼 코드 마스터

---

## 기술 스택

### 핵심 기술
- **언어**: Kotlin 1.9.25
- **Java**: 17
- **프레임워크**: Spring Boot 3.5.5
- **Spring Cloud**: 2025.0.0
- **반응형 스택**: Spring WebFlux, Reactor Core, Reactor Kotlin Extensions
- **데이터베이스**: PostgreSQL 15+ with R2DBC
- **빌드 도구**: Gradle

### 주요 의존성
```kotlin
dependencies {
    // Core
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0")
}
```

---

## 프로젝트 구조

```
bpmaster-manage-service/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com.interplug.bpmastermanageservice/
│   │   │       ├── BpmasterManageServiceApplication.kt
│   │   │       ├── config/
│   │   │       │   ├── R2dbcConfig.kt              # R2DBC 설정
│   │   │       │   └── SwaggerConfig.kt            # Swagger 설정
│   │   │       ├── controller/
│   │   │       │   ├── BpMasterController.kt
│   │   │       │   ├── BpContractInfoController.kt
│   │   │       │   ├── BpStoreInfoController.kt
│   │   │       │   ├── BpPfMappingController.kt
│   │   │       │   ├── BpMasterDataPermissionController.kt
│   │   │       │   └── PfCodeMasterController.kt
│   │   │       ├── entity/
│   │   │       │   ├── BpMaster.kt                 # BP 마스터 엔티티
│   │   │       │   ├── BpContractInfo.kt
│   │   │       │   ├── BpStoreInfo.kt
│   │   │       │   ├── BpPfMapping.kt
│   │   │       │   ├── BpMasterDataPermission.kt
│   │   │       │   └── PfCodeMaster.kt
│   │   │       ├── repository/
│   │   │       │   ├── BpMasterRepository.kt
│   │   │       │   ├── BpContractInfoRepository.kt
│   │   │       │   ├── BpStoreInfoRepository.kt
│   │   │       │   ├── BpPfMappingRepository.kt
│   │   │       │   ├── BpMasterDataPermissionRepository.kt
│   │   │       │   └── PfCodeMasterRepository.kt
│   │   │       └── service/
│   │   │           ├── BpMasterService.kt
│   │   │           ├── BpContractInfoService.kt
│   │   │           ├── BpStoreInfoService.kt
│   │   │           ├── BpPfMappingService.kt
│   │   │           ├── BpMasterDataPermissionService.kt
│   │   │           └── PfCodeMasterService.kt
│   │   └── resources/
│   │       ├── application.yml                     # 애플리케이션 설정
│   │       └── create_table.sql                   # 테이블 생성 스크립트
│   └── test/
│       └── kotlin/
├── docs/
│   └── CONVENTIONS.md                              # 코딩 컨벤션
├── build.gradle
└── README.md
```

---

## 주요 도메인

### 1. BP Master (비즈니스 파트너 마스터)

비즈니스 파트너의 기본 정보를 관리합니다.

**주요 필드**:
- `bpId`: BP ID (Primary Key)
- `bpCode`: BP 코드 (고유값)
- `bpName`: BP 명칭
- `bpType`: BP 유형 (VENDOR, CUSTOMER 등)
- `businessRegNo`: 사업자등록번호
- `representativeName`: 대표자명
- `primaryPfCode`: 주 플랫폼 코드
- `status`: 상태 (ACTIVE, INACTIVE 등)
- `erpUsageFee`: ERP 사용료
- `commissionRate`: 수수료율

### 2. BP Contract Info (계약 정보)

비즈니스 파트너 간 계약 정보를 관리합니다.

**주요 필드**:
- `contractId`: 계약 ID (Primary Key)
- `contractCode`: 계약 코드
- `contractorBpId`: 계약자 BP ID
- `contracteeBpId`: 피계약자 BP ID
- `contractType`: 계약 유형
- `contractStartDate`: 계약 시작일
- `contractEndDate`: 계약 종료일
- `feeRate`: 수수료율

### 3. BP Store Info (매장 정보)

비즈니스 파트너의 매장 정보를 관리합니다.

### 4. BP-PF Mapping (BP-PF 매핑)

비즈니스 파트너와 플랫폼 간의 매핑 관계를 관리합니다.

### 5. BP Master Data Permission (권한 관리)

비즈니스 파트너의 데이터 접근 권한을 관리합니다.

### 6. PF Code Master (플랫폼 코드 마스터)

플랫폼 코드 마스터 데이터를 관리합니다.

---

## API 엔드포인트

### Base URL
- **개발**: `http://localhost:{dynamic-port}/api/v1`
- **Eureka 통한 접근**: `http://bpmaster-manage-service/api/v1`

### BP Master APIs

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/bp-master` | 전체 BP 조회 (필터링 가능) | - | Flux\<BpMaster\> (200) |
| GET | `/bp-master/{id}` | BP 조회 (ID) | - | BpMaster (200) |
| GET | `/bp-master/code/{code}` | BP 조회 (코드) | - | BpMaster (200) |
| GET | `/bp-master/search?name={name}` | BP 검색 (이름) | - | Flux\<BpMaster\> (200) |
| POST | `/bp-master` | BP 생성 | BpMaster | BpMaster (201) |
| PUT | `/bp-master/{id}` | BP 수정 | BpMaster | BpMaster (200) |
| PATCH | `/bp-master/{id}/status` | BP 상태 변경 | status (query param) | BpMaster (200) |
| DELETE | `/bp-master/{id}` | BP 삭제 | - | Void (204) |
| GET | `/bp-master/check/business-reg-no/{businessRegNo}` | 사업자등록번호 중복 체크 | - | Map (200) |
| GET | `/bp-master/check/code/{code}` | BP 코드 중복 체크 | - | Map (200) |

**Query Parameters (GET /bp-master)**:
- `status`: 상태 필터 (ACTIVE, INACTIVE)
- `type`: BP 유형 필터 (VENDOR, CUSTOMER)
- `primaryPfCode`: 주 플랫폼 코드 필터

### BP Contract Info APIs
- Base Path: `/api/v1/bp-contract`
- CRUD 작업 지원

### BP Store Info APIs
- Base Path: `/api/v1/bp-store`
- CRUD 작업 지원

### BP-PF Mapping APIs
- Base Path: `/api/v1/bp-pf-mapping`
- CRUD 작업 지원

### BP Master Data Permission APIs
- Base Path: `/api/v1/bp-permission`
- CRUD 작업 지원

### PF Code Master APIs
- Base Path: `/api/v1/pf-code`
- CRUD 작업 지원

---

## 데이터베이스

### PostgreSQL 연결 설정

```yaml
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
      validation-query: SELECT 1
```

### 주요 테이블

#### bp_master
```sql
CREATE TABLE bp_master (
    bp_id BIGSERIAL PRIMARY KEY,
    bp_code VARCHAR(50) NOT NULL UNIQUE,
    bp_name VARCHAR(200) NOT NULL,
    bp_type VARCHAR(50) NOT NULL,
    business_reg_no VARCHAR(50),
    representative_name VARCHAR(100),
    address VARCHAR(500),
    phone_number VARCHAR(50),
    email VARCHAR(100),
    primary_pf_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    erp_usage_fee DECIMAL(15,2),
    commission_rate DECIMAL(5,2),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
```

#### bp_contract_info
```sql
CREATE TABLE bp_contract_info (
    contract_id BIGSERIAL PRIMARY KEY,
    contract_code VARCHAR(50) NOT NULL UNIQUE,
    contractor_bp_id BIGINT NOT NULL,
    contractee_bp_id BIGINT NOT NULL,
    contract_type VARCHAR(50) NOT NULL,
    pf_id BIGINT NOT NULL,
    contract_start_date DATE NOT NULL,
    contract_end_date DATE,
    contract_terms TEXT,
    fee_rate DECIMAL(5,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contractor_bp_id) REFERENCES bp_master(bp_id),
    FOREIGN KEY (contractee_bp_id) REFERENCES bp_master(bp_id)
);
```

### Entity 정의 (Kotlin Data Class)

```kotlin
@Table("bp_master")
data class BpMaster(
    @Id
    @Column("bp_id")
    val bpId: Long? = null,

    @Column("bp_code")
    val bpCode: String,

    @Column("bp_name")
    val bpName: String,

    @Column("bp_type")
    val bpType: String,

    @Column("business_reg_no")
    val businessRegNo: String? = null,

    @Column("status")
    val status: String? = "ACTIVE",

    @Column("erp_usage_fee")
    val erpUsageFee: BigDecimal? = null,

    @Column("commission_rate")
    val commissionRate: BigDecimal? = null,

    @CreatedDate
    @Column("created_date")
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_date")
    val updatedDate: LocalDateTime? = null
)
```

---

## 설정

### application.yml

```yaml
# Spring 설정
spring:
  application:
    name: bpmaster-manage-service

  # R2DBC PostgreSQL 설정
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/whaleerpdb
    username: whaleerp
    password: whaleerp12345!@
    pool:
      enabled: true
      initial-size: 5
      max-size: 20

  # Data 설정
  data:
    r2dbc:
      repositories:
        enabled: true

# Eureka Client 설정
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true

# 서버 설정
server:
  port: 0  # Random port

# Actuator 설정
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers,prometheus

# Resilience4j Circuit Breaker 설정
resilience4j:
  circuitbreaker:
    instances:
      bpmaster-service:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s

# 로깅 설정
logging:
  level:
    root: INFO
    com.interplug.bpmastermanageservice: DEBUG
    org.springframework.r2dbc: DEBUG

# SpringDoc OpenAPI 설정
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  api-docs:
    path: /v3/api-docs
    enabled: true
```

---

## 실행 방법

### 사전 요구사항

1. **Java 17 이상** 설치
2. **PostgreSQL 15+** 실행
   ```bash
   # Docker로 PostgreSQL 실행
   docker run -d \
     --name whaleerpdb \
     -e POSTGRES_USER=whaleerp \
     -e POSTGRES_PASSWORD=whaleerp12345!@ \
     -e POSTGRES_DB=whaleerpdb \
     -p 5432:5432 \
     postgres:15
   ```
3. **Eureka Server** 실행 (선택적)
   ```bash
   cd ../eureka && ./gradlew bootRun
   ```

### 테이블 생성

```bash
# create_table.sql 실행
psql -h localhost -U whaleerp -d whaleerpdb -f src/main/resources/create_table.sql
```

### 단계별 실행

#### 1. 빌드
```bash
cd bpmaster-manage-service
./gradlew clean build
```

#### 2. 실행
```bash
# Gradle을 통한 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/bpmaster-manage-service-0.0.1-SNAPSHOT.jar
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

### 1. BP Master 생성
```bash
curl -X POST http://localhost:{port}/api/v1/bp-master \
  -H "Content-Type: application/json" \
  -d '{
    "bpCode": "BP001",
    "bpName": "테스트 파트너",
    "bpType": "VENDOR",
    "businessRegNo": "123-45-67890",
    "representativeName": "홍길동",
    "address": "서울시 강남구",
    "phoneNumber": "02-1234-5678",
    "email": "test@example.com",
    "primaryPfCode": "PF001",
    "erpUsageFee": 100000.00,
    "commissionRate": 5.5
  }'
```

**Response (201 Created)**:
```json
{
  "bpId": 1,
  "bpCode": "BP001",
  "bpName": "테스트 파트너",
  "bpType": "VENDOR",
  "businessRegNo": "123-45-67890",
  "representativeName": "홍길동",
  "status": "ACTIVE",
  "createdDate": "2025-10-10T10:00:00",
  "updatedDate": "2025-10-10T10:00:00"
}
```

### 2. BP Master 조회
```bash
# 전체 조회
curl http://localhost:{port}/api/v1/bp-master

# 활성 BP만 조회
curl "http://localhost:{port}/api/v1/bp-master?status=ACTIVE"

# 타입별 조회
curl "http://localhost:{port}/api/v1/bp-master?type=VENDOR"

# ID로 조회
curl http://localhost:{port}/api/v1/bp-master/1

# 코드로 조회
curl http://localhost:{port}/api/v1/bp-master/code/BP001

# 이름으로 검색
curl "http://localhost:{port}/api/v1/bp-master/search?name=테스트"
```

### 3. BP Master 수정
```bash
curl -X PUT http://localhost:{port}/api/v1/bp-master/1 \
  -H "Content-Type: application/json" \
  -d '{
    "bpCode": "BP001",
    "bpName": "수정된 파트너",
    "bpType": "VENDOR",
    "businessRegNo": "123-45-67890",
    "representativeName": "김철수",
    "status": "ACTIVE"
  }'
```

### 4. BP Master 상태 변경
```bash
curl -X PATCH "http://localhost:{port}/api/v1/bp-master/1/status?status=INACTIVE&updatedBy=admin"
```

### 5. BP Master 삭제
```bash
curl -X DELETE http://localhost:{port}/api/v1/bp-master/1
```

**Response (204 No Content)**

### 6. 중복 체크
```bash
# 사업자등록번호 중복 체크
curl http://localhost:{port}/api/v1/bp-master/check/business-reg-no/123-45-67890

# BP 코드 중복 체크
curl http://localhost:{port}/api/v1/bp-master/check/code/BP001
```

**Response**:
```json
{
  "exists": false
}
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
2. 원하는 API 엔드포인트 선택 (예: BP Master)
3. `Try it out` 버튼 클릭
4. 필요한 파라미터 입력
5. `Execute` 버튼 클릭하여 API 호출

### Swagger 설정 (SwaggerConfig.kt)

```kotlin
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
                    Server().url("/").description("Current Server (Dynamic Port)"),
                    Server().url("http://localhost:8000").description("Via API Gateway")
                )
            )
}
```

---

## 반응형 프로그래밍

### Service Layer (Kotlin + Reactor)

```kotlin
@Service
class BpMasterService(
    private val bpMasterRepository: BpMasterRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 단건 조회 → Mono
    fun findById(id: Long): Mono<BpMaster> =
        bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))

    // 복수 조회 → Flux
    fun findAll(): Flux<BpMaster> {
        logger.debug("Finding all business partners")
        return bpMasterRepository.findAll()
    }

    // 생성
    @Transactional
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
            .doOnSuccess { logger.info("Successfully created business partner: ${it.bpCode}") }
            .doOnError { logger.error("Failed to create business partner: ${bpMaster.bpCode}", it) }
    }

    // 수정
    @Transactional
    fun update(id: Long, updateRequest: BpMaster): Mono<BpMaster> =
        bpMasterRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Business partner not found with id: $id")))
            .flatMap { existing ->
                val updated = existing.copy(
                    bpName = updateRequest.bpName,
                    bpType = updateRequest.bpType,
                    updatedDate = LocalDateTime.now()
                )
                bpMasterRepository.save(updated)
            }
            .doOnSuccess { logger.info("Successfully updated business partner id: $id") }
}
```

### Controller Layer (Kotlin)

```kotlin
@Tag(name = "BP Master", description = "Business Partner Master Data Management APIs")
@RestController
@RequestMapping("/api/v1/bp-master")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BpMasterController(
    private val bpMasterService: BpMasterService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAllBpMasters(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?
    ): Flux<BpMaster> {
        logger.info("GET /api/v1/bp-master - status: $status, type: $type")

        return when {
            status == "ACTIVE" && type != null -> bpMasterService.findActiveByType(type)
            status == "ACTIVE" -> bpMasterService.findAllActive()
            type != null -> bpMasterService.findByType(type)
            else -> bpMasterService.findAll()
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBpMaster(@RequestBody bpMaster: BpMaster): Mono<BpMaster> {
        logger.info("POST /api/v1/bp-master - Creating BP: ${bpMaster.bpCode}")
        return bpMasterService.create(bpMaster)
    }

    @GetMapping("/{id}")
    fun getBpMasterById(@PathVariable id: Long): Mono<ResponseEntity<BpMaster>> =
        bpMasterService.findById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
}
```

### Repository Layer (Kotlin)

```kotlin
@Repository
interface BpMasterRepository : ReactiveCrudRepository<BpMaster, Long> {

    // 메서드명으로 쿼리 생성
    fun findByBpCode(bpCode: String): Mono<BpMaster>
    fun findByBpType(bpType: String): Flux<BpMaster>
    fun findByStatus(status: String): Flux<BpMaster>
    fun findByBpTypeAndStatus(bpType: String, status: String): Flux<BpMaster>
    fun existsByBpCode(bpCode: String): Mono<Boolean>
    fun existsByBusinessRegNo(businessRegNo: String): Mono<Boolean>

    // @Query 어노테이션으로 커스텀 쿼리
    @Query("SELECT * FROM bp_master WHERE bp_name LIKE CONCAT('%', :bpName, '%')")
    fun searchByBpName(bpName: String): Flux<BpMaster>

    @Query("SELECT * FROM bp_master WHERE status = 'ACTIVE' ORDER BY bp_name")
    fun findAllActive(): Flux<BpMaster>

    @Query("SELECT * FROM bp_master WHERE bp_type = :bpType AND status = 'ACTIVE' ORDER BY bp_name")
    fun findActiveByType(bpType: String): Flux<BpMaster>
}
```

---

## Circuit Breaker

### Resilience4j 설정

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
    instances:
      bpmaster-service:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 3s
    instances:
      bpmaster-service:
        base-config: default
```

### Circuit Breaker 모니터링

```bash
# Circuit Breaker 상태 확인
curl http://localhost:{port}/actuator/circuitbreakers

# Health Check
curl http://localhost:{port}/actuator/health
```

---

## 트러블슈팅

### 일반적인 문제 해결

#### 1. PostgreSQL 연결 실패
**문제**: R2DBC 연결 오류
**해결**:
```bash
# PostgreSQL 실행 확인
docker ps | grep whaleerpdb

# 연결 테스트
psql -h localhost -U whaleerp -d whaleerpdb

# application.yml 확인
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/whaleerpdb
    username: whaleerp
    password: whaleerp12345!@
```

#### 2. 테이블 없음 오류
**문제**: "relation does not exist" 에러
**해결**:
```bash
# 테이블 생성 스크립트 실행
psql -h localhost -U whaleerp -d whaleerpdb -f src/main/resources/create_table.sql
```

#### 3. Eureka 등록 실패
**문제**: 서비스가 Eureka에 등록되지 않음
**해결**:
```bash
# Eureka Server 실행 확인
http://localhost:8761

# application.yml 확인
eureka:
  client:
    register-with-eureka: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### 4. 반응형 스트림 블로킹
**문제**: WebFlux에서 `.block()` 사용으로 인한 에러
**해결**: `.block()` 대신 반응형 체이닝 사용

```kotlin
// ❌ Bad
fun getBpMaster(id: Long): BpMaster {
    return bpMasterRepository.findById(id).block()!!  // 절대 사용 금지
}

// ✅ Good
fun getBpMaster(id: Long): Mono<BpMaster> =
    bpMasterRepository.findById(id)
        .switchIfEmpty(Mono.error(NoSuchElementException("Not found")))
```

#### 5. Kotlin Null Safety 이슈
**문제**: Nullable 타입 처리 오류
**해결**: Safe call(?.), Elvis 연산자(?:), let 사용

```kotlin
// ✅ Good - Safe call과 Elvis 연산자
val updatedBy = updateRequest.updatedBy ?: "system"

bpMaster.businessRegNo?.let { regNo ->
    bpMasterRepository.existsByBusinessRegNo(regNo)
} ?: Mono.just(false)
```

#### 6. Connection Pool 부족
**문제**: "Cannot acquire connection" 에러
**해결**: Connection Pool 크기 조정

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 5
      max-size: 20      # 증가
      max-idle-time: 30m
```

---

## 개발 가이드

### 코딩 컨벤션
- [Kotlin 코딩 컨벤션 가이드](./docs/CONVENTIONS.md) 참조

### 주요 원칙
1. **불변성**: `val` 사용 우선, `var`는 필요한 경우만
2. **Null 안전성**: Safe call, Elvis 연산자 적극 활용
3. **Data Class**: 엔티티, DTO는 data class로 정의
4. **반응형 프로그래밍**: 모든 I/O 작업은 비동기 처리
5. **에러 핸들링**: `switchIfEmpty`, `onErrorResume` 적극 활용

### 테스트 작성
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BpMasterServiceTests {

    @Autowired
    private lateinit var bpMasterRepository: BpMasterRepository

    @Test
    fun `should create and find business partner`() {
        val bpMaster = BpMaster(
            bpCode = "BP001",
            bpName = "Test Partner",
            bpType = "VENDOR"
        )

        StepVerifier.create(bpMasterRepository.save(bpMaster))
            .assertNext { saved ->
                assertNotNull(saved.bpId)
                assertEquals("BP001", saved.bpCode)
            }
            .verifyComplete()
    }
}
```

---

## 참고 자료

### 공식 문서
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/3.5.5/reference/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Reactor Kotlin Extensions](https://github.com/reactor/reactor-kotlin-extensions)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)
- [SpringDoc OpenAPI](https://springdoc.org/)

### 관련 문서
- [메인 프로젝트 CLAUDE.md](../CLAUDE.md) - MSA 전체 구조
- [BPMaster Service CLAUDE.md](./CLAUDE.md) - 상세 가이드
- [코딩 컨벤션 가이드](./docs/CONVENTIONS.md) - Kotlin 코딩 표준

---

## 라이선스

Copyright © 2025 Interplug. All rights reserved.

---

## 버전 정보

- **Version**: 0.0.1-SNAPSHOT
- **Kotlin**: 1.9.25
- **Java**: 21
- **Spring Boot**: 3.5.5
- **Spring Cloud**: 2025.0.0
- **Last Updated**: 2025-10-10
