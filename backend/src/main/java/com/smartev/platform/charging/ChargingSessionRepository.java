package com.smartev.platform.charging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findByUserIdOrderByStartTimeDesc(Long userId);

    Page<ChargingSession> findByUserId(Long userId, Pageable pageable);

    Optional<ChargingSession> findFirstByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<ChargingSession> findByReservationId(Long reservationId);

    long countByStatus(SessionStatus status);

    long countByUserIdAndStatus(Long userId, SessionStatus status);

    @Query("SELECT COALESCE(SUM(s.energyConsumedKwh), 0) FROM ChargingSession s")
    BigDecimal sumTotalEnergyDelivered();

    @Query("SELECT COALESCE(SUM(s.totalCost), 0) FROM ChargingSession s WHERE s.user.id = :userId")
    BigDecimal sumTotalCostByUserId(@Param("userId") Long userId);
}
