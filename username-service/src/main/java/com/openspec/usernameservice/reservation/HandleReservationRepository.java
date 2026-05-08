package com.openspec.usernameservice.reservation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface HandleReservationRepository extends CrudRepository<HandleReservation, String> {

    Optional<HandleReservation> findByEmail(String email);

    boolean existsByHandle(String handle);
}
