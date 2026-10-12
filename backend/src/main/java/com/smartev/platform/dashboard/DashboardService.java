package com.smartev.platform.dashboard;

import com.smartev.platform.charger.ChargerRepository;
import com.smartev.platform.charger.ChargerStatus;
import com.smartev.platform.charging.ChargingSession;
import com.smartev.platform.charging.ChargingSessionRepository;
import com.smartev.platform.charging.SessionResponse;
import com.smartev.platform.charging.SessionStatus;
import com.smartev.platform.reservation.Reservation;
import com.smartev.platform.reservation.ReservationRepository;
import com.smartev.platform.reservation.ReservationResponse;
import com.smartev.platform.reservation.ReservationStatus;
import com.smartev.platform.station.StationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ChargerRepository chargerRepository;
    private final StationRepository stationRepository;
    private final ReservationRepository reservationRepository;
    private final ChargingSessionRepository sessionRepository;

    public DashboardService(ChargerRepository chargerRepository,
                            StationRepository stationRepository,
                            ReservationRepository reservationRepository,
                            ChargingSessionRepository sessionRepository) {
        this.chargerRepository = chargerRepository;
        this.stationRepository = stationRepository;
        this.reservationRepository = reservationRepository;
        this.sessionRepository = sessionRepository;
    }

    public DashboardMetricsDTO getMetrics(Long userId) {
        long availableChargers = chargerRepository.countByStatus(ChargerStatus.AVAILABLE);
        long totalStations = stationRepository.count();
        long activeReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long activeSessions = sessionRepository.countByStatus(SessionStatus.CHARGING);

        BigDecimal totalEnergyDelivered = sessionRepository.sumTotalEnergyDelivered();
        BigDecimal userTotalCost = sessionRepository.sumTotalCostByUserId(userId);

        Optional<ChargingSession> activeSessionOpt = sessionRepository.findFirstByUserIdAndStatus(userId, SessionStatus.CHARGING);
        SessionResponse activeSession = activeSessionOpt.map(SessionResponse::new).orElse(null);

        List<ChargingSession> recentSessionsEntities = sessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        List<SessionResponse> recentSessions = recentSessionsEntities.stream()
                .limit(5)
                .map(SessionResponse::new)
                .collect(Collectors.toList());

        List<Reservation> recentReservationEntities = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ReservationResponse> recentReservations = recentReservationEntities.stream()
                .limit(5)
                .map(ReservationResponse::new)
                .collect(Collectors.toList());

        return new DashboardMetricsDTO(
                availableChargers,
                totalStations,
                activeReservations,
                activeSessions,
                totalEnergyDelivered != null ? totalEnergyDelivered : BigDecimal.ZERO,
                userTotalCost != null ? userTotalCost : BigDecimal.ZERO,
                activeSession,
                recentSessions,
                recentReservations
        );
    }
}
