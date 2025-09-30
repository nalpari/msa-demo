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

        Server currentServer = new Server()
                .url("/")
                .description("Current Server (Dynamic Port)");

        Server gatewayServer = new Server()
                .url("http://localhost:8000")
                .description("Via API Gateway");

        return new OpenAPI()
                .info(info)
                .addServersItem(currentServer)
                .addServersItem(gatewayServer);
    }
}