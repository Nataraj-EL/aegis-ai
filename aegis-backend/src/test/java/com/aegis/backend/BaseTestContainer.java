package com.aegis.backend;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public abstract class BaseTestContainer {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                    org.testcontainers.utility.DockerImageName.parse("ankane/pgvector:v0.5.1")
                            .asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("aegis_db")
            .withUsername("aegis_user")
            .withPassword("aegis_password");

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
