package com.interplug.bpmastermanageservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BPMaster Manage Service API")
                    .description("Business Partner Master Data Management Service API Documentation")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Interplug Development Team")
                            .email("dev@interplug.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("/")
                        .description("Current Server (Dynamic Port)"),
                    Server()
                        .url("http://localhost:8000")
                        .description("Via API Gateway")
                )
            )
    }
}