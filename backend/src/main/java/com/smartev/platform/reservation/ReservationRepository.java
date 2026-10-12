package com.smartev.platform.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Optional<Reservation> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Query("SELECT r FROM Reservation r WHERE r.charger.id = :chargerId AND r.status = 'CONFIRMED' AND r.expiryTime > :now")
    List<Reservation> findActiveByChargerId(@Param("chargerId") Long chargerId, @Param("now") Instant now);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.expiryTime < :now")
    List<Reservation> findExpiredReservations(@Param("now") Instant now);

    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    long countByStatus(ReservationStatus status);
}
