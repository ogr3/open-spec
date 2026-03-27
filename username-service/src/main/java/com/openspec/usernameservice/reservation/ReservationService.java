package com.openspec.usernameservice.reservation;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final HandleReservationRepository repository;

    public ReservationService(HandleReservationRepository repository) {
        this.repository = repository;
    }

    public Optional<String> reserveHandle(String email, Stream<String> candidates) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(candidates, "candidates must not be null");

        Iterator<String> iterator = candidates
                .filter(candidate -> candidate != null && !candidate.isBlank())
                .iterator();

        while (iterator.hasNext()) {
            String handle = iterator.next();
            if (tryReserve(handle, email)) {
                return Optional.of(handle);
            }
        }
        return Optional.empty();
    }

    public Optional<HandleReservation> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    private boolean tryReserve(String handle, String email) {
        try {
            repository.save(HandleReservation.newReservation(handle, email));
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }
}
