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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Tag(name = "Test Controller", description = "Test 리소스를 관리하는 API")
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
    public ResponseEntity<TestDto> createTest(@RequestBody TestDto testDto) {
        log.info("Creating new Test: {}", testDto.getName());
        TestDto created = testService.create(testDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Test 조회", description = "ID로 Test를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TestDto.class))),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public ResponseEntity<TestDto> getTestById(
            @Parameter(description = "조회할 Test의 ID", required = true)
            @PathVariable Long id) {
        log.info("Fetching Test with ID: {}", id);
        TestDto test = testService.findById(id);
        return ResponseEntity.ok(test);
    }

    @GetMapping
    @Operation(summary = "모든 Test 조회", description = "모든 Test 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<List<TestDto>> getAllTests() {
        log.info("Fetching all Tests");
        List<TestDto> tests = testService.findAll();
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/search")
    @Operation(summary = "Test 검색", description = "키워드로 Test를 검색합니다")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    public ResponseEntity<List<TestDto>> searchTests(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword) {
        log.info("Searching Tests with keyword: {}", keyword);
        List<TestDto> tests = testService.findByNameContaining(keyword);
        return ResponseEntity.ok(tests);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Test 수정", description = "기존 Test를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TestDto.class))),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public ResponseEntity<TestDto> updateTest(
            @Parameter(description = "수정할 Test의 ID", required = true)
            @PathVariable Long id,
            @RequestBody TestDto testDto) {
        log.info("Updating Test with ID: {}", id);
        TestDto updated = testService.update(id, testDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Test 삭제", description = "Test를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "Test를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteTest(
            @Parameter(description = "삭제할 Test의 ID", required = true)
            @PathVariable Long id) {
        log.info("Deleting Test with ID: {}", id);
        testService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Feign Client 통신 예제
    @GetMapping("/{testId}/user/{userId}")
    @Operation(summary = "Test와 User 조회", description = "Test와 연관된 User 정보를 함께 조회합니다 (Feign Client 사용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음"),
            @ApiResponse(responseCode = "503", description = "User 서비스 불가능")
    })
    public ResponseEntity<UserDto> getTestWithUser(
            @Parameter(description = "Test ID", required = true)
            @PathVariable Long testId,
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        log.info("Fetching Test {} with User {}", testId, userId);

        // Test 조회
        TestDto test = testService.findById(testId);
        log.info("Found Test: {}", test.getName());

        // Feign Client를 통한 User 서비스 호출
        UserDto user = userServiceClient.getUserById(userId);
        log.info("Found User via Feign: {}", user != null ? user.getName() : "null");

        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    @Operation(summary = "모든 User 조회", description = "Feign Client를 통해 User 서비스의 모든 사용자를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "503", description = "User 서비스 불가능")
    })
    public ResponseEntity<List<UserDto>> getAllUsersViaFeign() {
        log.info("Fetching all users via Feign Client");
        List<UserDto> users = userServiceClient.getAllUsers();
        log.info("Found {} users via Feign", users != null ? users.size() : 0);
        return ResponseEntity.ok(users);
    }
}