package com.smartev.platform.reservation;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.charger.ChargerRepository;
import com.smartev.platform.charger.ChargerStatus;
import com.smartev.platform.common.exception.BadRequestException;
import com.smartev.platform.common.exception.ConflictException;
import com.smartev.platform.common.exception.ForbiddenException;
import com.smartev.platform.common.exception.ResourceNotFoundException;
import com.smartev.platform.station.StationStatus;
import com.smartev.platform.user.Role;
import com.smartev.platform.user.User;
import com.smartev.platform.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ChargerRepository chargerRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.chargerRepository = chargerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Idempotency Check: if key provided and already exists for user, return existing reservation
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            Optional<Reservation> existing = reservationRepository.findByUserIdAndIdempotencyKey(userId, request.getIdempotencyKey().trim());
            if (existing.isPresent()) {
                log.info("Idempotent reservation request detected for user {} with key {}", userId, request.getIdempotencyKey());
                return new ReservationResponse(existing.get());
            }
        }

        // 2. Fetch Charger with Pessimistic Write Lock to prevent race conditions
        Charger charger = chargerRepository.findByIdForUpdate(request.getChargerId())
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with id: " + request.getChargerId()));

        // 3. Validate Station Operational Status
        if (charger.getStation().getOperationalStatus() != StationStatus.OPERATIONAL) {
            throw new ConflictException("Charging station is currently " + charger.getStation().getOperationalStatus());
        }

        // 4. Validate Charger Availability
        if (charger.getStatus() != ChargerStatus.AVAILABLE) {
            throw new ConflictException("Charger #" + charger.getId() + " is currently " + charger.getStatus() + " and cannot be reserved");
        }

        // 5. Create Reservation
        Instant startTime = Instant.now();
        int minutes = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) ? request.getDurationMinutes() : 30;
        Instant expiryTime = startTime.plus(Duration.ofMinutes(minutes));

        Reservation reservation = new Reservation(
                user,
                charger,
                startTime,
                expiryTime,
                (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) ? request.getIdempotencyKey().trim() : null
        );

        // 6. Transition Charger State to RESERVED
        charger.setStatus(ChargerStatus.RESERVED);
        chargerRepository.save(charger);

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation #{} created for user #{} on charger #{}", savedReservation.getId(), userId, charger.getId());

        return new ReservationResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!reservation.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to view this reservation");
        }

        return new ReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!reservation.getUser().getId().equals(userId) && user.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("You are not authorized to cancel this reservation");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Reservation cannot be cancelled in state: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Release Charger if it was reserved by this reservation
        Charger charger = reservation.getCharger();
        if (charger.getStatus() == ChargerStatus.RESERVED) {
            charger.setStatus(ChargerStatus.AVAILABLE);
            chargerRepository.save(charger);
        }

        log.info("Reservation #{} cancelled by user #{}", reservationId, userId);
        return new ReservationResponse(reservation);
    }

    /**
     * Periodic background job to auto-expire reservations past their expiry time
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cleanupExpiredReservations() {
        Instant now = Instant.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(now);
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            Charger charger = reservation.getCharger();
            if (charger.getStatus() == ChargerStatus.RESERVED) {
                charger.setStatus(ChargerStatus.AVAILABLE);
                chargerRepository.save(charger);
                log.info("Auto-expired reservation #{} and released charger #{}", reservation.getId(), charger.getId());
            }
        }
    }
}
