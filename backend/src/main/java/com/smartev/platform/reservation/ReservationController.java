package com.smartev.platform.reservation;

import com.smartev.platform.common.ApiResponse;
import com.smartev.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Endpoints for reserving EV chargers and managing booking lifecycle")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new charger reservation with concurrency locking and optional idempotency")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Reservation created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List reservations for the authenticated user")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserReservations(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ReservationResponse> reservations = reservationService.getUserReservations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Reservations retrieved successfully", reservations));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get details of a specific reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long reservationId) {
        ReservationResponse reservation = reservationService.getReservationById(currentUser.getId(), reservationId);
        return ResponseEntity.ok(ApiResponse.ok("Reservation details retrieved successfully", reservation));
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancel an active charger reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long reservationId) {
        ReservationResponse response = reservationService.cancelReservation(currentUser.getId(), reservationId);
        return ResponseEntity.ok(ApiResponse.ok("Reservation cancelled successfully", response));
    }
}
