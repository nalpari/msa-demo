package com.interplug.testservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Test 데이터 전송 객체")
public class TestDto {

    @Schema(description = "Test ID", example = "1")
    private Long id;

    @Schema(description = "Test 이름", example = "테스트 이름", required = true)
    private String name;

    @Schema(description = "Test 설명", example = "테스트 설명입니다")
    private String description;

    @Schema(description = "생성 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}