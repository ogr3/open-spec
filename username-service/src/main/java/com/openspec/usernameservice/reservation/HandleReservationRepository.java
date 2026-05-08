package com.openspec.usernameservice.reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface HandleReservationRepository extends CrudRepository<HandleReservation, String> {

    Optional<HandleReservation> findByEmail(String email);

    boolean existsByHandle(String handle);

    @Query("SELECT \"handle\", \"email\", \"created_at\" FROM \"handles\" ORDER BY \"created_at\" DESC LIMIT :limit")
    List<HandleReservation> findRecent(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM \"handles\" WHERE \"created_at\" < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
