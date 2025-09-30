package com.interplug.testservice.config;

import com.interplug.testservice.entity.Test;
import com.interplug.testservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
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
                                .name("WebFlux Reactive Test")
                                .description("Spring WebFlux를 사용한 반응형 프로그래밍 테스트")
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        Test.builder()
                                .name("R2DBC Integration Test")
                                .description("R2DBC를 사용한 반응형 데이터베이스 접근 테스트")
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        Test.builder()
                                .name("WebClient Communication Test")
                                .description("WebClient를 사용한 마이크로서비스 간 통신 테스트")
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        Test.builder()
                                .name("Eureka Discovery Test")
                                .description("Eureka 서비스 디스커버리를 통한 동적 서비스 찾기 테스트")
                                .createdAt(now)
                                .updatedAt(now)
                                .build(),
                        Test.builder()
                                .name("Reactive Circuit Breaker Test")
                                .description("Resilience4j를 사용한 반응형 Circuit Breaker 패턴 테스트")
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ))
                .flatMap(testRepository::save)
                .doOnNext(test -> log.info("Initialized Test: {}", test.getName()))
                .doOnComplete(() -> log.info("Test data initialization completed"))
                .blockLast();  // 초기화 완료까지 대기
    }
}