package com.barbearia.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Sobe um PostgreSQL 16 efêmero (mesma imagem do compose) para os testes de
 * integração. O @ServiceConnection injeta a conexão no DataSource do Spring,
 * e o Flyway aplica as migrations do zero a cada contexto.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgres() {
		return new PostgreSQLContainer<>("postgres:16");
	}
}
