package com.openspec.usernameservice.reservation;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;

@Table("handles")
public class HandleReservation {

    @Id
    private final String handle;

    @Column("email")
    private final String email;

    @Column("created_at")
    private final Instant createdAt;

    public HandleReservation(String handle, String email, Instant createdAt) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.email = Objects.requireNonNull(email, "email");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static HandleReservation newReservation(String handle, String email) {
        return new HandleReservation(handle, email, Instant.now());
    }

    public String getHandle() {
        return handle;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
