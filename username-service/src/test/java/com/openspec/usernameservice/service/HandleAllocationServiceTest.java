package com.openspec.usernameservice.service;

import com.openspec.usernameservice.blocklist.ProfanityFilter;
import com.openspec.usernameservice.domain.HandleGenerationService;
import com.openspec.usernameservice.reservation.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleAllocationServiceTest {

    @Mock
    private HandleGenerationService generationService;

    @Mock
    private ProfanityFilter profanityFilter;

    @Mock
    private ReservationService reservationService;

    private HandleAllocationService service;

    @BeforeEach
    void setUp() {
        service = new HandleAllocationService(generationService, profanityFilter, reservationService);
    }

    @Test
    void returnsHandleWhenReservationSucceeds() {
        when(generationService.generateCandidates("alice@example.se")).thenReturn(Stream.of("ALA"));
        when(profanityFilter.isBlocked("ALA")).thenReturn(false);
        when(reservationService.reserve("ALA", "alice@example.se")).thenReturn(true);

        var response = service.allocate("alice@example.se");

        assertThat(response.handle()).isEqualTo("ALA");
    }

    @Test
    void throwsAllBlockedWhenNoCandidateAllowed() {
        when(generationService.generateCandidates("blocked@example.se")).thenReturn(Stream.of("KUK"));
        when(profanityFilter.isBlocked("KUK")).thenReturn(true);

        assertThatThrownBy(() -> service.allocate("blocked@example.se"))
                .isInstanceOf(HandleAllocationService.HandleAllocationException.class)
                .hasMessageContaining("Unable to allocate handle")
                .extracting("code")
                .isEqualTo("all_blocked");
    }

    @Test
    void throwsCollisionsWhenAllReserved() {
        when(generationService.generateCandidates("taken@example.se")).thenReturn(Stream.of("ALA", "ALA1"));
        when(profanityFilter.isBlocked("ALA")).thenReturn(false);
        when(profanityFilter.isBlocked("ALA1")).thenReturn(false);
        when(reservationService.reserve("ALA", "taken@example.se")).thenReturn(false);
        when(reservationService.reserve("ALA1", "taken@example.se")).thenReturn(false);

        assertThatThrownBy(() -> service.allocate("taken@example.se"))
                .isInstanceOf(HandleAllocationService.HandleAllocationException.class)
                .extracting("code")
                .isEqualTo("collisions_exhausted");
    }
}
