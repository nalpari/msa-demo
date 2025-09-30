# Swagger UI Setup Guide for BPMaster Manage Service

## 📚 Overview
BPMaster Manage Service는 SpringDoc OpenAPI를 사용하여 자동화된 API 문서를 제공합니다.

## 🚀 Setup Completed

### 1. Dependencies Added
```gradle
// build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'
```

### 2. Configuration
- **SwaggerConfig.kt**: OpenAPI Bean 설정
- **application.yml**: SpringDoc 설정 추가

### 3. Annotations
주요 Controller에 Swagger 어노테이션 추가:
- `@Tag`: Controller 레벨 문서화
- `@Operation`: 메소드 레벨 설명
- `@Parameter`: 파라미터 설명
- `@ApiResponses`: 응답 코드 문서화

## 📍 Access Points

### Local Development
- **Swagger UI**: http://localhost:[PORT]/swagger-ui.html
- **API Docs (JSON)**: http://localhost:[PORT]/v3/api-docs
- **API Docs (YAML)**: http://localhost:[PORT]/v3/api-docs.yaml

### Via Gateway (After Eureka Registration)
- **Swagger UI**: http://localhost:8080/bpmaster-manage-service/swagger-ui.html
- **API Docs**: http://localhost:8080/bpmaster-manage-service/v3/api-docs

## 🎯 Features

### Configured Settings
- **Operations Sorter**: Method (GET, POST, PUT, DELETE 순서)
- **Tags Sorter**: Alphabetical
- **Authorization**: JWT Bearer Token 지원
- **Request Duration**: API 응답 시간 표시
- **Persist Authorization**: 인증 정보 유지

### API Groups
1. **BP Master**: Business Partner 관리
2. **BP PF Mapping**: BP-PF 매핑 관리
3. **PF Code Master**: PF 코드 관리
4. **BP Contract Info**: 계약 정보 관리
5. **BP Master Data Permission**: 데이터 권한 관리
6. **BP Store Info**: 매장 정보 관리

## 🔐 Security

JWT 인증을 위한 Bearer Token 설정:
1. Swagger UI 우측 상단 "Authorize" 버튼 클릭
2. Bearer Token 입력 (format: `Bearer <your-jwt-token>`)
3. "Authorize" 클릭

## 📝 Usage Example

### 1. 서비스 시작
```bash
cd bpmaster-manage-service
./gradlew bootRun
```

### 2. 포트 확인
서비스가 랜덤 포트로 시작되므로, 콘솔에서 포트 확인:
```
Started BpmasterManageServiceApplication in X seconds (process running for Y)
Netty started on port XXXXX
```

### 3. Swagger UI 접속
브라우저에서 `http://localhost:XXXXX/swagger-ui.html` 접속

## 🔧 Customization

### 추가 Controller 문서화
```kotlin
@Tag(name = "Your API", description = "Your API Description")
@RestController
class YourController {

    @Operation(
        summary = "Operation summary",
        description = "Detailed description"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Success"),
            ApiResponse(responseCode = "400", description = "Bad Request")
        ]
    )
    @GetMapping("/your-endpoint")
    fun yourMethod(
        @Parameter(description = "Parameter description")
        @RequestParam param: String
    ): Mono<YourResponse> {
        // Implementation
    }
}
```

## 📌 Notes
- WebFlux 환경이므로 모든 응답은 `Mono` 또는 `Flux`로 래핑됩니다
- 랜덤 포트 사용으로 Eureka를 통한 접근이 권장됩니다
- CORS는 모든 origin에 대해 열려있으므로 프로덕션에서는 보안 설정이 필요합니다