package by.osinovi.userservice.integration.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user_service_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withStartupTimeout(java.time.Duration.ofSeconds(150))
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1));

    @Container
    protected static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:7.4.2-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(java.time.Duration.ofSeconds(150))
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "?sslmode=disable");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> false);
    }
}