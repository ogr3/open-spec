package com.openspec.usernameservice.reservation;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface HandleReservationRepository extends CrudRepository<HandleReservation, String> {

    Optional<HandleReservation> findByEmail(String email);

    boolean existsByHandle(String handle);
}
