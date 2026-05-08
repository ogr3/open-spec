package com.openspec.usernameservice.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class ReservationPersistenceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private HandleReservationRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"handles\" (\"handle\" VARCHAR(8) PRIMARY KEY, \"email\" TEXT NOT NULL, \"created_at\" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        repository.deleteAll();
    }

    @Test
    void savesNewReservationWhenIdIsPreassigned() {
        boolean reserved = reservationService.reserve("ALA", "alice@example.se");

        assertThat(reserved).isTrue();
        assertThat(repository.findById("ALA")).isPresent();
    }

    @Test
    void findRecentReturnsNewestFirstAndRespectsLimit() {
        jdbcTemplate.update(
                "INSERT INTO \"handles\"(\"handle\", \"email\", \"created_at\") VALUES (?, ?, ?)",
                "OLD",
                "old@example.se",
                Instant.now().minus(3, ChronoUnit.DAYS));
        jdbcTemplate.update(
                "INSERT INTO \"handles\"(\"handle\", \"email\", \"created_at\") VALUES (?, ?, ?)",
                "NEW",
                "new@example.se",
                Instant.now().minus(1, ChronoUnit.DAYS));

        List<HandleReservation> reservations = repository.findRecent(1);

        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getHandle()).isEqualTo("NEW");
    }

    @Test
    void deleteOlderThanRemovesOnlyEntriesBeforeCutoff() {
        Instant cutoff = Instant.now().minus(2, ChronoUnit.DAYS);
        jdbcTemplate.update(
                "INSERT INTO \"handles\"(\"handle\", \"email\", \"created_at\") VALUES (?, ?, ?)",
                "OLD",
                "old@example.se",
                cutoff.minus(1, ChronoUnit.HOURS));
        jdbcTemplate.update(
                "INSERT INTO \"handles\"(\"handle\", \"email\", \"created_at\") VALUES (?, ?, ?)",
                "NEW",
                "new@example.se",
                cutoff.plus(1, ChronoUnit.HOURS));

        int deletedCount = repository.deleteOlderThan(cutoff);

        assertThat(deletedCount).isEqualTo(1);
        assertThat(repository.findById("OLD")).isEmpty();
        assertThat(repository.findById("NEW")).isPresent();
    }
}
