package com.openspec.usernameservice.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private HandleReservationRepository repository;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(repository);
    }

    @Test
    void reserveHandleSkipsInvalidCandidatesAndReturnsFirstSuccessfulReservation() {
        when(repository.save(any(HandleReservation.class)))
                .thenThrow(new DuplicateKeyException("duplicate"))
                .thenReturn(HandleReservation.newReservation("BOB", "alice@example.se"));

        Optional<String> result = service.reserveHandle(
                "alice@example.se",
                Stream.of(null, "", "   ", "ALA", "BOB"));

        assertThat(result).contains("BOB");
    }

    @Test
    void reserveHandleReturnsEmptyWhenAllCandidatesCollide() {
        when(repository.save(any(HandleReservation.class))).thenThrow(new DuplicateKeyException("duplicate"));

        Optional<String> result = service.reserveHandle("alice@example.se", Stream.of("ALA", "BOB"));

        assertThat(result).isEmpty();
    }

    @Test
    void reserveReturnsFalseWhenDuplicateKeyOccurs() {
        when(repository.save(any(HandleReservation.class))).thenThrow(new DuplicateKeyException("duplicate"));

        boolean reserved = service.reserve("ALA", "alice@example.se");

        assertThat(reserved).isFalse();
    }

    @Test
    void findByEmailDelegatesToRepository() {
        HandleReservation reservation = new HandleReservation("ALA", "alice@example.se", Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.findByEmail("alice@example.se")).thenReturn(Optional.of(reservation));

        Optional<HandleReservation> found = service.findByEmail("alice@example.se");

        assertThat(found).contains(reservation);
        verify(repository).findByEmail("alice@example.se");
    }

    @Test
    void reserveHandleRejectsNullEmail() {
        assertThatThrownBy(() -> service.reserveHandle(null, Stream.of("ALA")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("email must not be null");
    }

    @Test
    void reserveHandleRejectsNullCandidates() {
        assertThatThrownBy(() -> service.reserveHandle("alice@example.se", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("candidates must not be null");
    }

    @Test
    void reservePropagatesUnexpectedDataAccessExceptions() {
        when(repository.save(any(HandleReservation.class)))
                .thenThrow(new DataAccessResourceFailureException("database offline"));

        assertThatThrownBy(() -> service.reserve("ALA", "alice@example.se"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("database offline");
    }
}
