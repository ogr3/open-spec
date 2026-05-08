package com.openspec.usernameservice.reservation;

import java.util.Optional;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final HandleReservationRepository repository;

    public ReservationService(final HandleReservationRepository repository) {
        this.repository = repository;
    }

    public Optional<String> reserveHandle(final @NonNull String email, final @NonNull Stream<String> candidates) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(candidates, "candidates must not be null");
        return candidates
                .filter(candidate -> candidate != null && !candidate.isBlank())
                .filter(handle -> tryReserve(handle, email))
                .findFirst();
    }

    public Optional<HandleReservation> findByEmail(final @NonNull String email) {
        Objects.requireNonNull(email, "email must not be null");
        return repository.findByEmail(email);
    }

    public boolean reserve(final @NonNull String handle, final @NonNull String email) {
        Objects.requireNonNull(handle, "handle must not be null");
        Objects.requireNonNull(email, "email must not be null");
        return tryReserve(handle, email);
    }

    private boolean tryReserve(final @NonNull String handle, final @NonNull String email) {
        try {
            repository.save(HandleReservation.newReservation(handle, email));
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }
}
