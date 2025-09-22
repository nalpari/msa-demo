package com.interplug.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.commons.codec.digest.DigestUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class TokenKeyConfig {

    @Bean
    public KeyResolver tokenKeyResolver() {
        return exchange -> {
            var cookies = exchange.getRequest().getCookies().getFirst(TokenType.ACCESS.getName());
            if (cookies != null) {
                return Mono.just(cookies.getValue());
            } else {
                String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress();
                String hashedIp = DigestUtils.sha256Hex(clientIp);
                return Mono.just(hashedIp);
            }
        };
    }
}
