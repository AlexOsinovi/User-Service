package by.osinovi.userservice.integration.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
public abstract class BaseIntegrationTests {

    @Container
    protected PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user_service_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withStartupTimeout(java.time.Duration.ofSeconds(120))
            .withReuse(false);

    @Container
    protected GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
            .withExposedPorts(6379)
            .withStartupTimeout(java.time.Duration.ofSeconds(120))
            .withReuse(false);

    @BeforeEach
    void startContainers() {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        if (!redis.isRunning()) {
            redis.start();
        }
    }

    @AfterEach
    void stopContainers() {
        try {
            if (redis.isRunning()) {
                redis.stop();
                redis.close();
            }
            if (postgres.isRunning()) {
                postgres.stop();
                postgres.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to stop containers: " + e.getMessage());
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("user_service_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withStartupTimeout(java.time.Duration.ofSeconds(120))
                .withReuse(false);
        if (!postgres.isRunning()) {
            postgres.start();
        }

        GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
                .withExposedPorts(6379)
                .withStartupTimeout(java.time.Duration.ofSeconds(120))
                .withReuse(false);
        if (!redis.isRunning()) {
            redis.start();
        }

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}