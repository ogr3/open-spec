package com.openspec.usernameservice.reservation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ENABLE_DOCKER_TESTS", matches = "true")
class ReservationServiceIntegrationTest {

    static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        try {
            if (postgres == null) {
                postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                        .withDatabaseName("usernames")
                        .withUsername("username")
                        .withPassword("secret");
                postgres.start();
            }
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.flyway.enabled", () -> true);
        } catch (Throwable ex) {
            Assumptions.assumeTrue(false, "Docker not available: " + ex.getMessage());
        }
    }

    @AfterAll
    static void shutdown() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private HandleReservationRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void reservesFirstAvailableCandidate() {
        Optional<String> handle = reservationService.reserveHandle("alice@example.se", Stream.of("ALA", "ALA1"));

        assertThat(handle).contains("ALA");
        assertThat(repository.findById("ALA")).isPresent();
    }

    @Test
    void retriesOnCollisions() {
        reservationService.reserveHandle("peter@example.se", Stream.of("ALA"));

        Optional<String> second = reservationService.reserveHandle("mia@example.se", Stream.of("ALA", "ALA1", "ALA2"));

        assertThat(second).contains("ALA1");
        assertThat(repository.findById("ALA1")).isPresent();
    }

    @Test
    void returnsEmptyWhenAllCandidatesReserved() {
        reservationService.reserveHandle("first@example.se", Stream.of("ALA"));

        Optional<String> result = reservationService.reserveHandle("second@example.se", Stream.of("ALA"));

        assertThat(result).isEmpty();
    }

}
