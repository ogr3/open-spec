package com.openspec.usernameservice.reservation;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("handles")
public class HandleReservation implements Persistable<String> {

    @Id
    @Column("handle")
    private final String handle;

    @Column("email")
    private final String email;

    @Column("created_at")
    private final Instant createdAt;

    @Transient
    private final boolean isNew;

    @PersistenceCreator
    public HandleReservation(String handle, String email, Instant createdAt) {
        this(handle, email, createdAt, false);
    }

    private HandleReservation(String handle, String email, Instant createdAt, boolean isNew) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.email = Objects.requireNonNull(email, "email");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.isNew = isNew;
    }

    public static HandleReservation newReservation(String handle, String email) {
        return new HandleReservation(handle, email, Instant.now(), true);
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

    @Override
    public String getId() {
        return handle;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
