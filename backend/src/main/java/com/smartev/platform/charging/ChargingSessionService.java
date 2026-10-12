package com.smartev.platform.charging;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.charger.ChargerRepository;
import com.smartev.platform.charger.ChargerStatus;
import com.smartev.platform.common.exception.BadRequestException;
import com.smartev.platform.common.exception.ForbiddenException;
import com.smartev.platform.common.exception.ResourceNotFoundException;
import com.smartev.platform.reservation.Reservation;
import com.smartev.platform.reservation.ReservationRepository;
import com.smartev.platform.reservation.ReservationStatus;
import com.smartev.platform.user.Role;
import com.smartev.platform.user.User;
import com.smartev.platform.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargingSessionService {

    private static final Logger log = LoggerFactory.getLogger(ChargingSessionService.class);

    private final ChargingSessionRepository sessionRepository;
    private final ReservationRepository reservationRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final TelemetrySimulator telemetrySimulator;

    public ChargingSessionService(ChargingSessionRepository sessionRepository,
                                  ReservationRepository reservationRepository,
                                  ChargerRepository chargerRepository,
                                  UserRepository userRepository,
                                  TelemetrySimulator telemetrySimulator) {
        this.sessionRepository = sessionRepository;
        this.reservationRepository = reservationRepository;
        this.chargerRepository = chargerRepository;
        this.userRepository = userRepository;
        this.telemetrySimulator = telemetrySimulator;
    }

    @Transactional
    public SessionResponse startSession(Long userId, Long reservationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (!reservation.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to start a session for this reservation");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Reservation is not eligible for charging. Current status: " + reservation.getStatus());
        }

        Charger charger = chargerRepository.findByIdForUpdate(reservation.getCharger().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with id: " + reservation.getCharger().getId()));

        // Mark reservation as consumed
        reservation.setStatus(ReservationStatus.CONSUMED);
        reservationRepository.save(reservation);

        // Transition charger to CHARGING
        charger.setStatus(ChargerStatus.CHARGING);
        chargerRepository.save(charger);

        // Create charging session
        ChargingSession session = new ChargingSession(reservation, user, charger, Instant.now(), 22.0);
        ChargingSession savedSession = sessionRepository.save(session);

        log.info("Started charging session #{} for user #{} on charger #{}", savedSession.getId(), userId, charger.getId());
        return new SessionResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream()
                .map(SessionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long userId, Long sessionId) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!session.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to view this session");
        }

        return new SessionResponse(session);
    }

    @Transactional(readOnly = true)
    public TelemetryData getSessionTelemetry(Long userId, Long sessionId) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!session.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to view telemetry for this session");
        }

        return telemetrySimulator.computeTelemetry(session, session.getCharger(), Instant.now());
    }

    @Transactional
    public SessionResponse endSession(Long userId, Long sessionId) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!session.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to terminate this session");
        }

        if (session.getStatus() != SessionStatus.CHARGING) {
            throw new BadRequestException("Session is not currently active. Current status: " + session.getStatus());
        }

        Instant endTime = Instant.now();
        Charger charger = chargerRepository.findByIdForUpdate(session.getCharger().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with id: " + session.getCharger().getId()));

        TelemetryData finalTelemetry = telemetrySimulator.computeTelemetry(session, charger, endTime);

        session.setEndTime(endTime);
        session.setStatus(SessionStatus.COMPLETED);
        session.setCurrentBatteryPct(finalTelemetry.getCurrentBatteryPct());
        session.setEnergyConsumedKwh(finalTelemetry.getEnergyDeliveredKwh());
        session.setTotalCost(finalTelemetry.getCurrentCost());

        ChargingSession completedSession = sessionRepository.save(session);

        // Free charger back to AVAILABLE
        charger.setStatus(ChargerStatus.AVAILABLE);
        chargerRepository.save(charger);

        log.info("Completed charging session #{}. Energy: {} kWh, Total cost: ${}",
                sessionId, completedSession.getEnergyConsumedKwh(), completedSession.getTotalCost());

        return new SessionResponse(completedSession);
    }
}
