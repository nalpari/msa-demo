package com.interplug.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> responseBody = new HashMap<>();
        if (ex instanceof HttpStatusCodeException statusEx) {
            exchange.getResponse().setStatusCode(statusEx.getStatusCode());
            responseBody.put("status", statusEx.getStatusCode());
            responseBody.put("message", statusEx.getMessage());
        } else {
            exchange.getResponse()
                    .setStatusCode(ErrorType.TOKEN_AUTHORIZATION_FAIL.getStatusCode());
            responseBody.put("status", ErrorType.TOKEN_AUTHORIZATION_FAIL.getStatusCode());
            responseBody.put("message", ex.getMessage());
        }

        DataBuffer wrap = null;
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
            wrap = exchange.getResponse().bufferFactory().wrap(bytes);
        } catch (JsonProcessingException e) {
            log.error("fatal error : {}", e.getMessage());
        }

        return exchange.getResponse().writeWith(Flux.just(Objects.requireNonNull(wrap)));
    }
}
