package com.interplug.testservice.controller;

import com.interplug.testservice.client.UserServiceClient;
import com.interplug.testservice.dto.TestDto;
import com.interplug.testservice.dto.UserDto;
import com.interplug.testservice.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Test Controller", description = "Test 리소스를 관리하는 API (Reactive)")
public class TestController {

    private final TestService testService;
    private final UserServiceClient userServiceClient;

    @PostMapping
    @Operation(summary = "Test 생성", description = "새로운 Test를 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Test 생성 성공",
                    content = @Content(schema = @Schema(implementation = TestDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public Mono<ResponseEntity<TestDto>> createTest(@RequestBody TestDto testDto) {
        log.info("Creating new Test: {}", testDto.getName());
        return testService.create(testDto)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Test 조회", description = "ID로 Test를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TestDto.class))),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public Mono<ResponseEntity<TestDto>> getTestById(
            @Parameter(description = "조회할 Test의 ID", required = true)
            @PathVariable Long id) {
        log.info("Fetching Test with ID: {}", id);
        return testService.findById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @GetMapping
    @Operation(summary = "모든 Test 조회", description = "모든 Test 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public Flux<TestDto> getAllTests() {
        log.info("Fetching all Tests");
        return testService.findAll();
    }

    @GetMapping("/search")
    @Operation(summary = "Test 검색", description = "키워드로 Test를 검색합니다")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    public Flux<TestDto> searchTests(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword) {
        log.info("Searching Tests with keyword: {}", keyword);
        return testService.findByNameContaining(keyword);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Test 수정", description = "기존 Test를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TestDto.class))),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public Mono<ResponseEntity<TestDto>> updateTest(
            @Parameter(description = "수정할 Test의 ID", required = true)
            @PathVariable Long id,
            @RequestBody TestDto testDto) {
        log.info("Updating Test with ID: {}", id);
        return testService.update(id, testDto)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error updating test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Test 삭제", description = "Test를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public Mono<ResponseEntity<Void>> deleteTest(
            @Parameter(description = "삭제할 Test의 ID", required = true)
            @PathVariable Long id) {
        log.info("Deleting Test with ID: {}", id);
        return testService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting test: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    // WebClient 통신 예제
    @GetMapping("/{testId}/user/{userId}")
    @Operation(summary = "Test와 User 조회", description = "Test와 연관된 User 정보를 함께 조회합니다 (WebClient 사용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "503", description = "User 서비스 불가능")
    })
    public Mono<ResponseEntity<UserDto>> getTestWithUser(
            @Parameter(description = "Test ID", required = true)
            @PathVariable Long testId,
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        log.info("Fetching Test {} with User {}", testId, userId);

        return testService.findById(testId)
                .doOnNext(test -> log.info("Found Test: {}", test.getName()))
                .flatMap(test -> userServiceClient.getUserById(userId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching test or user: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/users")
    @Operation(summary = "모든 User 조회", description = "WebClient를 통해 User 서비스의 모든 사용자를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "503", description = "User 서비스 불가능")
    })
    public Flux<UserDto> getAllUsersViaWebClient() {
        log.info("Fetching all users via WebClient");
        return userServiceClient.getAllUsers()
                .doOnComplete(() -> log.info("Completed fetching users"));
    }
}