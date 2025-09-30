package com.interplug.testservice.service;

import com.interplug.testservice.dto.TestDto;
import com.interplug.testservice.entity.Test;
import com.interplug.testservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

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

    public Mono<TestDto> findById(Long id) {
        return testRepository.findById(id)
                .map(this::convertToDto)
                .switchIfEmpty(Mono.error(
                    new RuntimeException("Test not found with ID: " + id)
                ));
    }

    public Flux<TestDto> findAll() {
        return testRepository.findAll()
                .doOnComplete(() -> log.info("Retrieved all test entities"))
                .map(this::convertToDto);
    }

    public Flux<TestDto> findByNameContaining(String keyword) {
        return testRepository.findByNameContaining(keyword)
                .doOnComplete(() -> log.info("Search completed for keyword: {}", keyword))
                .map(this::convertToDto);
    }

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

    private TestDto convertToDto(Test test) {
        return TestDto.builder()
                .id(test.getId())
                .name(test.getName())
                .description(test.getDescription())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
    }
}