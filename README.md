# Spring Cloud MSA Demo

Spring Cloud 기반 마이크로서비스 아키텍처 데모 프로젝트

## 📋 프로젝트 개요

이 프로젝트는 Spring Cloud를 활용한 마이크로서비스 아키텍처 구현 예제입니다. Config Server, Service Discovery (Eureka), API Gateway, 그리고 샘플 마이크로서비스를 포함한 완전한 MSA 생태계를 제공합니다.

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                         사용자 요청                          │
└─────────────────┬───────────────────────────────────────────┘
                  ▼
        ┌─────────────────┐
        │   API Gateway   │ (Port: 8080)
        │   Spring Cloud   │ - JWT 인증/인가
        │     Gateway     │ - 라우팅 & 필터링
        └────────┬────────┘
                 │
                 ▼
        ┌─────────────────┐
        │  Eureka Server  │ (Port: 8761)
        │ Service Registry│ - 서비스 디스커버리
        └────────┬────────┘ - 서비스 등록/조회
                 │
      ┌──────────┴──────────┐
      ▼                     ▼
┌──────────────┐    ┌──────────────┐
│ Test Service │    │ Other Service│
│  (Random Port)│    │  (Future)    │
└──────────────┘    └──────────────┘
        │
        ▼
┌─────────────────┐
│  Config Server  │ (Port: 8888)
│ Centralized     │ - Git 기반 설정 관리
│ Configuration   │ - SSH 인증
└─────────────────┘
```

## 🚀 기술 스택

### 핵심 기술
- **Java**: 24 (toolchain)
- **Spring Boot**: 3.5.5
- **Spring Cloud**: 2025.0.0
- **Build Tool**: Gradle with Spring Dependency Management

### 마이크로서비스별 스택

#### Config Server (포트: 8888)
- Spring Cloud Config Server
- Spring Security (Basic Auth)
- Git 기반 설정 저장소 연동
- SSH 키 인증

#### Eureka Server (포트: 8761)
- Netflix Eureka Server
- Spring Security
- Spring Cloud Config Client
- Service Registry & Discovery

#### API Gateway (포트: 8080)
- Spring Cloud Gateway (WebFlux)
- JWT 인증/인가 (jjwt)
- Eureka Client
- Reactive Programming

#### Test Service (포트: 랜덤)
- Spring Web (REST API)
- Spring Data JPA
- H2 Database (In-Memory)
- Eureka Client

## 📦 프로젝트 구조

```
msa-demo/
├── config-server/      # 중앙 설정 관리 서버
├── eureka/            # 서비스 디스커버리 서버
├── gateway/           # API 게이트웨이
├── test-service/      # 샘플 마이크로서비스
└── build.gradle       # 루트 프로젝트 설정
```

## 🔧 설치 및 실행

### 사전 요구사항
- JDK 24
- Gradle 8.x
- Git

### 실행 순서 (중요!)

서비스 간 의존성 때문에 아래 순서대로 실행해야 합니다:

#### 1. Config Server 실행
```bash
cd config-server
./gradlew bootRun
```
- http://localhost:8888 에서 실행
- 인증 정보: devgrr / qwer1234

#### 2. Eureka Server 실행
```bash
cd eureka
./gradlew bootRun
```
- http://localhost:8761 에서 Eureka Dashboard 확인 가능
- Config Server에서 설정을 가져옴

#### 3. API Gateway 실행
```bash
cd gateway
./gradlew bootRun
```
- http://localhost:8080 에서 실행
- 모든 API 요청의 진입점

#### 4. 마이크로서비스 실행
```bash
cd test-service
./gradlew bootRun
```
- 랜덤 포트에서 실행 (충돌 방지)
- Eureka에 자동 등록

### 전체 빌드
```bash
# 루트 디렉토리에서
./gradlew clean build
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 서비스 테스트
cd [service-name]
./gradlew test
```

## 🔐 보안 설정

### Config Server
- GitHub 저장소: `git@github.com:nalpari/config-server.git`
- SSH 키 기반 인증
- Basic Auth: devgrr / qwer1234

### API Gateway
- JWT 기반 인증/인가
- 토큰 검증 및 라우팅

### Eureka Server
- Spring Security로 보호
- Config Server를 통한 인증 정보 관리

## 🔌 서비스 간 통신

1. **서비스 등록**: 각 마이크로서비스는 시작 시 Eureka Server에 자동 등록
2. **서비스 디스커버리**: Gateway가 Eureka를 통해 서비스 위치 조회
3. **설정 관리**: 모든 서비스가 Config Server에서 설정 로드
4. **API 라우팅**: Gateway가 요청을 적절한 서비스로 라우팅

## 🛠 개발 환경 설정

### IDE 설정
- Lombok 플러그인 설치 필요
- Java 24 SDK 설정
- Spring Boot DevTools 활성화 (Hot Reload)

### 로컬 개발 팁
- 각 서비스는 독립적으로 개발 가능
- `spring.profiles.active=dev`로 개발 프로파일 사용
- DevTools로 코드 변경 시 자동 재시작

## 📊 모니터링

### Actuator Endpoints
모든 서비스에 Spring Boot Actuator가 포함되어 있어 다음 엔드포인트 사용 가능:
- `/actuator/health` - 서비스 상태 확인
- `/actuator/info` - 서비스 정보
- `/actuator/metrics` - 메트릭 정보

### Eureka Dashboard
- URL: http://localhost:8761
- 등록된 모든 서비스 인스턴스 확인 가능
- 서비스 상태 실시간 모니터링

## 🚧 트러블슈팅

### Config Server 연결 실패
- Config Server가 먼저 실행되었는지 확인
- 인증 정보 확인: devgrr / qwer1234
- Git 저장소 접근 권한 확인

### Eureka 등록 실패
- Eureka Server 실행 상태 확인
- 네트워크 연결 상태 점검
- 서비스 설정의 eureka.client.service-url 확인

### Gateway 라우팅 실패
- 대상 서비스가 Eureka에 등록되었는지 확인
- Gateway 라우팅 설정 검토
- JWT 토큰 유효성 확인

## 📚 참고 자료

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Cloud Gateway Guide](https://spring.io/projects/spring-cloud-gateway)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)

## 📝 라이선스

이 프로젝트는 학습 및 데모 목적으로 제작되었습니다.

---

**Created with Spring Boot 3.5.5 and Spring Cloud 2025.0.0**