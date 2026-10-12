package com.smartev.platform.charger;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {

    List<Charger> findByStationId(Long stationId);

    long countByStatus(ChargerStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Charger c WHERE c.id = :id")
    Optional<Charger> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT c FROM Charger c WHERE c.station.id = :stationId " +
           "AND (:connectorType IS NULL OR c.connectorType = :connectorType) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:minPower IS NULL OR c.maxPowerKw >= :minPower)")
    List<Charger> findByStationWithFilters(
            @Param("stationId") Long stationId,
            @Param("connectorType") ConnectorType connectorType,
            @Param("status") ChargerStatus status,
            @Param("minPower") Double minPower
    );
}
