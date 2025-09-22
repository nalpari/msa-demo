package com.interplug.testservice.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            log.debug("Feign Request: {} {}", requestTemplate.method(), requestTemplate.url());
            // 필요시 헤더 추가 (예: 인증 토큰)
            // requestTemplate.header("Authorization", "Bearer " + token);
            requestTemplate.header("X-Request-Source", "test-service");
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            switch (response.status()) {
                case 404:
                    log.error("404 Error for method: {}", methodKey);
                    return new RuntimeException("Resource not found: " + methodKey);
                case 500:
                    log.error("500 Error for method: {}", methodKey);
                    return new RuntimeException("Internal server error: " + methodKey);
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
}