package com.smartev.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartev.platform.reservation.ReservationRequest;
import com.smartev.platform.reservation.ReservationResponse;
import com.smartev.platform.security.JwtTokenProvider;
import com.smartev.platform.security.UserPrincipal;
import com.smartev.platform.user.Role;
import com.smartev.platform.user.User;
import com.smartev.platform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ReservationAndChargingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;
    private String secondUserToken;
    private User testUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("demo@smartev.com")
                .orElseGet(() -> userRepository.save(new User("Demo User", "demo@smartev.com", "hash", Role.ROLE_USER)));
        userToken = "Bearer " + jwtTokenProvider.generateToken(UserPrincipal.create(testUser));

        secondUser = userRepository.findByEmail("other@smartev.com")
                .orElseGet(() -> userRepository.save(new User("Other User", "other@smartev.com", "hash", Role.ROLE_USER)));
        secondUserToken = "Bearer " + jwtTokenProvider.generateToken(UserPrincipal.create(secondUser));
    }

    @Test
    @DisplayName("Complete Journey: Reserve -> Idempotent check -> Conflict check -> Start Session -> Live Telemetry -> End Session -> Dashboard Metrics")
    void testEndToEndChargingJourney() throws Exception {
        Long targetChargerId = 2L; // Silicon Valley Supercharger NACS 250kW
        String idempotencyKey = UUID.randomUUID().toString();

        // 1. Create Reservation
        ReservationRequest reservationRequest = new ReservationRequest(targetChargerId, 45, idempotencyKey);
        MvcResult resResult = mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.chargerId").value(targetChargerId))
                .andReturn();

        String resJson = resResult.getResponse().getContentAsString();
        Long reservationId = objectMapper.readTree(resJson).path("data").path("id").asLong();
        assertNotNull(reservationId);

        // 2. Idempotency Check: Repeating request with identical key returns the SAME reservation
        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(reservationId));

        // 3. Concurrency / Conflict Check: Another user attempting to reserve the same charger gets 409 CONFLICT
        ReservationRequest conflictingRequest = new ReservationRequest(targetChargerId, 30, UUID.randomUUID().toString());
        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));

        // 4. Start Charging Session from valid reservation
        MvcResult startResult = mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/start")
                        .header("Authorization", userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CHARGING"))
                .andReturn();

        String startJson = startResult.getResponse().getContentAsString();
        Long sessionId = objectMapper.readTree(startJson).path("data").path("id").asLong();
        assertNotNull(sessionId);

        // 5. Verify Invalid Transition: Attempting to start the consumed reservation again fails with 400 Bad Request
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/start")
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));

        // 6. Check Live Telemetry Status
        mockMvc.perform(get("/api/v1/sessions/" + sessionId + "/status")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CHARGING"))
                .andExpect(jsonPath("$.data.simulated").value(true))
                .andExpect(jsonPath("$.data.currentBatteryPct", greaterThanOrEqualTo(20.0)));

        // 7. Ownership Security: Second user cannot end this user's session
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/end")
                        .header("Authorization", secondUserToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        // 8. End Charging Session
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/end")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalCost", notNullValue()));

        // 9. Verify Charger is released and available again
        mockMvc.perform(get("/api/v1/chargers/" + targetChargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        // 10. Check Dashboard Metrics for authenticated user
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableChargers", greaterThan(0)))
                .andExpect(jsonPath("$.data.recentSessions", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should cancel confirmed reservation and release charger")
    void testCancelReservation() throws Exception {
        Long targetChargerId = 4L; // Downtown GreenCharge Type 2
        ReservationRequest request = new ReservationRequest(targetChargerId, 30, UUID.randomUUID().toString());

        MvcResult resResult = mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long reservationId = objectMapper.readTree(resResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Cancel Reservation
        mockMvc.perform(post("/api/v1/reservations/" + reservationId + "/cancel")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Verify charger status reverted to AVAILABLE
        mockMvc.perform(get("/api/v1/chargers/" + targetChargerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }
}
