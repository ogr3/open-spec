package com.openspec.usernameservice.reservation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class AdminReservationService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    private final HandleReservationRepository repository;

    public AdminReservationService(final @NonNull HandleReservationRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public List<HandleReservation> listRecent(final int requestedLimit) {
        final int limit = requestedLimit <= 0 ? DEFAULT_LIMIT : Math.min(requestedLimit, MAX_LIMIT);
        return repository.findRecent(limit);
    }

    public int deleteOlderThanDays(final int olderThanDays) {
        if (olderThanDays <= 0) {
            throw new IllegalArgumentException("olderThanDays must be greater than zero");
        }
        final Instant cutoff = Instant.now().minus(olderThanDays, ChronoUnit.DAYS);
        return repository.deleteOlderThan(cutoff);
    }
}
