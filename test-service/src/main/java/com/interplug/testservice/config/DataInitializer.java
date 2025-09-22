package com.interplug.testservice.config;

import com.interplug.testservice.entity.Test;
import com.interplug.testservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner init(TestRepository testRepository) {
        return args -> {
            // 기존 데이터 삭제
            testRepository.deleteAll();
            log.info("Cleared existing test data");

            // 샘플 데이터 생성
            Test test1 = Test.builder()
                    .name("OpenFeign Integration Test")
                    .description("Spring Cloud OpenFeign을 사용한 마이크로서비스 간 통신 테스트")
                    .build();

            Test test2 = Test.builder()
                    .name("Eureka Discovery Test")
                    .description("Eureka 서비스 디스커버리를 통한 동적 서비스 찾기 테스트")
                    .build();

            Test test3 = Test.builder()
                    .name("Circuit Breaker Test")
                    .description("Resilience4j를 사용한 Circuit Breaker 패턴 테스트")
                    .build();

            Test test4 = Test.builder()
                    .name("Load Balancing Test")
                    .description("Spring Cloud LoadBalancer를 통한 클라이언트 사이드 로드밸런싱 테스트")
                    .build();

            Test test5 = Test.builder()
                    .name("Config Server Test")
                    .description("Spring Cloud Config를 통한 중앙화된 설정 관리 테스트")
                    .build();

            // 데이터 저장
            testRepository.saveAll(Arrays.asList(test1, test2, test3, test4, test5));
            log.info("Initialized {} sample test records", 5);

            // 저장된 데이터 확인
            testRepository.findAll().forEach(test -> {
                log.info("Created Test: {} - {}", test.getName(), test.getDescription());
            });
        };
    }
}