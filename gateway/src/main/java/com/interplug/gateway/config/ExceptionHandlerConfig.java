package com.interplug.gateway.config;

import com.interplug.gateway.exception.GlobalExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionHandlerConfig {

    @Bean
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
