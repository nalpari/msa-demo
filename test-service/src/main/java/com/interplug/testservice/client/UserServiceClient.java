package com.interplug.testservice.client;

import com.interplug.testservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "user-service",  // Eureka에 등록된 서비스 이름
        fallback = UserServiceFallback.class,
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    List<UserDto> getAllUsers();

    @PostMapping("/api/users")
    UserDto createUser(@RequestBody UserDto userDto);

    @PutMapping("/api/users/{id}")
    UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserDto userDto);

    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}