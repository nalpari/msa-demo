package com.interplug.gateway.config;

public enum TokenType {
    ACCESS("access_token"),
    REFRESH("refresh_token");

    private final String name;

    TokenType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}