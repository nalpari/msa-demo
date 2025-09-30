package com.interplug.bpmastermanageservice.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.interplug.bpmastermanageservice.repository"])
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig : AbstractR2dbcConfiguration() {

    // ConnectionFactory는 Spring Boot의 자동 설정을 사용하므로 별도 구현 불필요
    override fun connectionFactory(): ConnectionFactory {
        // Spring Boot auto-configuration에서 제공
        throw UnsupportedOperationException("ConnectionFactory is provided by Spring Boot auto-configuration")
    }

    // 트랜잭션 매니저 설정
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}