package com.openspec.usernameservice.api;

import java.time.Instant;

public record AdminHandleReservationResponse(String handle, String email, Instant createdAt) {}
