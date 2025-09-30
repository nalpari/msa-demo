package com.interplug.testservice.client;

import com.interplug.testservice.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080/api/users")  // Gateway를 통한 라우팅
                // 또는 Eureka 연동: .baseUrl("http://user-service/api/users")
                .build();
    }

    /**
     * User ID로 사용자 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 Mono (없으면 empty)
     */
    public Mono<UserDto> getUserById(Long userId) {
        return webClient.get()
                .uri("/{id}", userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", user != null ? user.getName() : "null"))
                .onErrorResume(e -> {
                    log.error("Error fetching user {}: {}", userId, e.getMessage());
                    return Mono.empty();  // Fallback: 빈 결과 반환
                });
    }

    /**
     * 모든 사용자 조회
     * @return 사용자 목록 Flux
     */
    public Flux<UserDto> getAllUsers() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(UserDto.class)
                .timeout(Duration.ofSeconds(5))
                .doOnComplete(() -> log.info("Successfully fetched all users"))
                .onErrorResume(e -> {
                    log.error("Error fetching all users: {}", e.getMessage());
                    return Flux.empty();  // Fallback: 빈 목록 반환
                });
    }

    /**
     * 사용자 생성
     * @param userDto 생성할 사용자 정보
     * @return 생성된 사용자 정보 Mono
     */
    public Mono<UserDto> createUser(UserDto userDto) {
        return webClient.post()
                .bodyValue(userDto)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(user -> log.info("Successfully created user: {}", user.getName()))
                .onErrorResume(e -> {
                    log.error("Error creating user: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * 사용자 수정
     * @param userId 수정할 사용자 ID
     * @param userDto 수정할 사용자 정보
     * @return 수정된 사용자 정보 Mono
     */
    public Mono<UserDto> updateUser(Long userId, UserDto userDto) {
        return webClient.put()
                .uri("/{id}", userId)
                .bodyValue(userDto)
                .retrieve()
                .bodyToMono(UserDto.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(user -> log.info("Successfully updated user: {}", user.getName()))
                .onErrorResume(e -> {
                    log.error("Error updating user {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * 사용자 삭제
     * @param userId 삭제할 사용자 ID
     * @return 삭제 완료 Mono
     */
    public Mono<Void> deleteUser(Long userId) {
        return webClient.delete()
                .uri("/{id}", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSuccess(v -> log.info("Successfully deleted user: {}", userId))
                .onErrorResume(e -> {
                    log.error("Error deleting user {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }
}