package com.interplug.testservice.service;

import com.interplug.testservice.dto.TestDto;
import com.interplug.testservice.entity.Test;
import com.interplug.testservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestService {

    private final TestRepository testRepository;

    @Transactional
    public TestDto create(TestDto testDto) {
        Test test = Test.builder()
                .name(testDto.getName())
                .description(testDto.getDescription())
                .build();

        test = testRepository.save(test);
        log.info("Created Test entity with ID: {}", test.getId());
        return convertToDto(test);
    }

    public TestDto findById(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));
        return convertToDto(test);
    }

    public List<TestDto> findAll() {
        List<Test> tests = testRepository.findAll();
        log.info("Found {} test entities", tests.size());
        return tests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TestDto> findByNameContaining(String keyword) {
        List<Test> tests = testRepository.findByNameContaining(keyword);
        log.info("Found {} test entities with keyword: {}", tests.size(), keyword);
        return tests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TestDto update(Long id, TestDto testDto) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));

        test.setName(testDto.getName());
        test.setDescription(testDto.getDescription());

        test = testRepository.save(test);
        log.info("Updated Test entity with ID: {}", test.getId());
        return convertToDto(test);
    }

    @Transactional
    public void delete(Long id) {
        if (!testRepository.existsById(id)) {
            throw new RuntimeException("Test not found with ID: " + id);
        }
        testRepository.deleteById(id);
        log.info("Deleted Test entity with ID: {}", id);
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