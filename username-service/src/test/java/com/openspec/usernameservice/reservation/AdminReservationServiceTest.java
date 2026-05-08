package com.openspec.usernameservice.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private HandleReservationRepository repository;

    @Test
    void usesDefaultLimitWhenRequestLimitIsZero() {
        AdminReservationService service = new AdminReservationService(repository);
        when(repository.findRecent(100)).thenReturn(List.of());

        service.listRecent(0);

        verify(repository).findRecent(100);
    }

    @Test
    void capsLimitToMaxSize() {
        AdminReservationService service = new AdminReservationService(repository);
        when(repository.findRecent(1000)).thenReturn(List.of());

        service.listRecent(5000);

        verify(repository).findRecent(1000);
    }

    @Test
    void deletesMappingsOlderThanGivenDays() {
        AdminReservationService service = new AdminReservationService(repository);
        when(repository.deleteOlderThan(any(Instant.class))).thenReturn(2);

        int deleted = service.deleteOlderThanDays(14);

        assertThat(deleted).isEqualTo(2);
        verify(repository).deleteOlderThan(any(Instant.class));
    }

    @Test
    void rejectsNonPositiveDays() {
        AdminReservationService service = new AdminReservationService(repository);

        assertThatThrownBy(() -> service.deleteOlderThanDays(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("olderThanDays");
    }
}
