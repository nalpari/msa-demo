# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 가이드를 제공합니다.

## 프로젝트 개요

Java 24, Spring Boot 3.5.5, Spring Cloud 2025.0.0으로 구축된 마이크로서비스 데모 프로젝트

## 아키텍처

### 마이크로서비스 구성

1. **config-server** (8888)
   - 중앙화된 설정 관리 (git@github.com:nalpari/config-server.git)
   - Spring Security Basic 인증

2. **eureka** (8761)
   - 서비스 디스커버리 서버
   - Spring Security 보안 적용

3. **gateway** (8080)
   - API 라우팅 및 필터링
   - JWT 기반 인증/인가

4. **test-service** (랜덤 포트)
   - H2 인메모리 DB 사용 샘플 서비스

## 주요 명령어

### 빌드
```bash
./gradlew build              # 전체 빌드
./gradlew clean build        # 클린 빌드
```

### 실행 (순서 중요!)
```bash
cd config-server && ./gradlew bootRun  # 1. 설정 서버
cd eureka && ./gradlew bootRun         # 2. 유레카
cd gateway && ./gradlew bootRun        # 3. 게이트웨이
cd test-service && ./gradlew bootRun   # 4. 마이크로서비스
```

### 테스트
```bash
./gradlew test               # 전체 테스트
```

## 기술 스택

- **Java**: 24
- **Spring Boot**: 3.5.5
- **Spring Cloud**: 2025.0.0
- **빌드**: Gradle
- **DB**: H2
- **보안**: Spring Security, JWT
- **기타**: Lombok, DevTools

## 중요 사항

- 서비스 시작 순서: config-server → eureka → gateway → 마이크로서비스
- Config Server 인증: devgrr:qwer1234
- Test 서비스는 포트 충돌 방지를 위해 랜덤 포트 사용 (port: 0)