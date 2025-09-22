package com.interplug.gateway.filter;

import com.interplug.gateway.config.CookieProvider;
import com.interplug.gateway.config.JwtProvider;
import com.interplug.gateway.config.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private final CookieProvider cookieProvider;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthorizationFilter(CookieProvider cookieProvider, JwtProvider jwtProvider) {
        super(Config.class);
        this.cookieProvider = cookieProvider;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String token = cookieProvider.getTokenFromCookies(request.getCookies());

            jwtProvider.validateToken(token, TokenType.ACCESS);

            Long memberId = jwtProvider.getMemberIdByToken(token, TokenType.ACCESS);

            ServerHttpRequest newRequest = request.mutate()
                    .header("member-id", String.valueOf(memberId)).build();

            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    public static class Config {

    }
}
