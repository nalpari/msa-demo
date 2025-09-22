package com.interplug.testservice.client;

import com.interplug.testservice.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserDto getUserById(Long id) {
        log.warn("Fallback: getUserById called for id: {}", id);
        return UserDto.builder()
                .id(id)
                .username("fallback-user")
                .name("Fallback User")
                .email("fallback@example.com")
                .role("GUEST")
                .build();
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.warn("Fallback: getAllUsers called");
        List<UserDto> fallbackUsers = new ArrayList<>();
        fallbackUsers.add(UserDto.builder()
                .id(0L)
                .username("fallback-user")
                .name("Fallback User")
                .email("fallback@example.com")
                .role("GUEST")
                .build());
        return fallbackUsers;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.warn("Fallback: createUser called");
        userDto.setId(0L);
        userDto.setRole("GUEST");
        return userDto;
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.warn("Fallback: updateUser called for id: {}", id);
        userDto.setId(id);
        userDto.setRole("GUEST");
        return userDto;
    }

    @Override
    public void deleteUser(Long id) {
        log.warn("Fallback: deleteUser called for id: {}", id);
        // Fallback에서는 아무 동작도 하지 않음
    }
}