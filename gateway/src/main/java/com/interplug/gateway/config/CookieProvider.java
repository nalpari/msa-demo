package com.interplug.gateway.config;

import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class CookieProvider {

    public String getTokenFromCookies(MultiValueMap<String, HttpCookie> cookies) {
        if (cookies.isEmpty()) {
            return null;
        }
        return cookies.get(TokenType.ACCESS.getName()).stream().map(HttpCookie::getValue)
                .findAny()
                .orElse(null);
    }
}
