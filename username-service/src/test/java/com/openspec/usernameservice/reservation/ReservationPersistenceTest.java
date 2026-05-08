package com.openspec.usernameservice.reservation;

import static org.assertj.core.api.Assertions.assertThat;

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
}
