package com.openspec.usernameservice.service;

import com.openspec.usernameservice.api.HandleResponse;
import com.openspec.usernameservice.blocklist.ProfanityFilter;
import com.openspec.usernameservice.domain.HandleGenerationService;
import com.openspec.usernameservice.reservation.ReservationService;
import org.springframework.stereotype.Service;

@Service
public class HandleAllocationService {

    private final HandleGenerationService generationService;
    private final ProfanityFilter profanityFilter;
    private final ReservationService reservationService;

    public HandleAllocationService(
            HandleGenerationService generationService,
            ProfanityFilter profanityFilter,
            ReservationService reservationService) {
        this.generationService = generationService;
        this.profanityFilter = profanityFilter;
        this.reservationService = reservationService;
    }

    public HandleResponse allocate(String email) {
        if (reservationService.findByEmail(email).isPresent()) {
            throw new HandleAllocationException("email_already_reserved", "Email already has a reserved handle", email);
        }

        boolean sawAllowed = false;
        var iterator = generationService.generateCandidates(email).iterator();

        while (iterator.hasNext()) {
            String candidate = iterator.next();
            if (profanityFilter.isBlocked(candidate)) {
                continue;
            }
            sawAllowed = true;
            if (reservationService.reserve(candidate, email)) {
                return new HandleResponse(candidate, true);
            }
        }

        if (!sawAllowed) {
            throw new HandleAllocationException("all_blocked", "Unable to allocate handle", email);
        }
        throw new HandleAllocationException("collisions_exhausted", "Unable to allocate handle", email);
    }

    public static class HandleAllocationException extends RuntimeException {
        private final String code;
        private final String email;

        public HandleAllocationException(String code, String message, String email) {
            super(message);
            this.code = code;
            this.email = email;
        }

        public String getCode() {
            return code;
        }

        public String getEmail() {
            return email;
        }
    }
}
