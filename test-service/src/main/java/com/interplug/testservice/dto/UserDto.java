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
@Schema(description = "User 데이터 전송 객체 (Feign Client 응답)")
public class UserDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자명", example = "user123")
    private String username;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String role;

    @Schema(description = "생성 일시", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}