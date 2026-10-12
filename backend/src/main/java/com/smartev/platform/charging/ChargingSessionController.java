package com.smartev.platform.charging;

import com.smartev.platform.common.ApiResponse;
import com.smartev.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Charging Sessions", description = "Endpoints for managing charging sessions and live telemetry")
public class ChargingSessionController {

    private final ChargingSessionService sessionService;

    public ChargingSessionController(ChargingSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/reservations/{reservationId}/start")
    @Operation(summary = "Start charging session from an active confirmed reservation")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long reservationId) {
        SessionResponse response = sessionService.startSession(currentUser.getId(), reservationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Charging session initiated successfully", response));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get charging sessions history for the current user")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<SessionResponse> sessions = sessionService.getUserSessions(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Charging sessions retrieved successfully", sessions));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get specific charging session details")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        SessionResponse session = sessionService.getSessionById(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session details retrieved successfully", session));
    }

    @GetMapping("/sessions/{sessionId}/status")
    @Operation(summary = "Get live telemetry (battery SoC %, energy delivered, current cost, elapsed time)")
    public ResponseEntity<ApiResponse<TelemetryData>> getSessionTelemetry(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        TelemetryData telemetry = sessionService.getSessionTelemetry(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session telemetry retrieved successfully", telemetry));
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End an ongoing charging session and finalize billing")
    public ResponseEntity<ApiResponse<SessionResponse>> endSession(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        SessionResponse response = sessionService.endSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Charging session ended successfully", response));
    }
}
