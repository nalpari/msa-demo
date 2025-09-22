package com.interplug.testservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Test Service API")
                .version("v1.0")
                .description("Test Service API 문서")
                .contact(new Contact()
                        .name("DevGrr")
                        .email("devgrr@interplug.com"));

        Server localServer = new Server()
                .url("http://localhost:8080/test-service")
                .description("Gateway를 통한 접근");

        Server directServer = new Server()
                .url("http://localhost:0")
                .description("직접 서비스 접근 (랜덤 포트)");

        return new OpenAPI()
                .info(info)
                .addServersItem(localServer)
                .addServersItem(directServer);
    }
}