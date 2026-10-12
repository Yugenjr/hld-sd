package com.smartev.platform.station;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<ChargingStation, Long> {

    @Query("SELECT s FROM ChargingStation s WHERE " +
           "(:query IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:status IS NULL OR s.operationalStatus = :status)")
    Page<ChargingStation> findWithFilters(
            @Param("query") String query,
            @Param("status") StationStatus status,
            Pageable pageable
    );

    List<ChargingStation> findByOperationalStatus(StationStatus status);
}
