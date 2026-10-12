package com.smartev.platform;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.charger.ChargerStatus;
import com.smartev.platform.charger.ConnectorType;
import com.smartev.platform.charging.ChargingSession;
import com.smartev.platform.charging.SessionStatus;
import com.smartev.platform.charging.TelemetryData;
import com.smartev.platform.charging.TelemetrySimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TelemetrySimulatorTest {

    private TelemetrySimulator simulator;
    private Charger testCharger;

    @BeforeEach
    void setUp() {
        simulator = new TelemetrySimulator();
        testCharger = new Charger(null, ConnectorType.CCS2, 150.0, ChargerStatus.CHARGING, BigDecimal.valueOf(0.40));
    }

    @Test
    @DisplayName("Should advance battery SoC and energy delivered deterministically")
    void testTelemetryProgression() {
        Instant startTime = Instant.now().minus(60, ChronoUnit.SECONDS);
        ChargingSession session = new ChargingSession(null, null, testCharger, startTime, 20.0);

        TelemetryData telemetry = simulator.computeTelemetry(session, testCharger, Instant.now());

        assertNotNull(telemetry);
        assertTrue(telemetry.getElapsedSeconds() >= 60);
        assertTrue(telemetry.getCurrentBatteryPct() > 20.0);
        assertTrue(telemetry.getEnergyDeliveredKwh().doubleValue() > 0.0);
        assertTrue(telemetry.getCurrentCost().doubleValue() > 0.0);
        assertTrue(telemetry.isSimulated());
    }

    @Test
    @DisplayName("Should cap battery State of Charge at 100.0% max")
    void testBatteryCapAtHundredPercent() {
        // Simulate long charging duration (e.g., 5 hours)
        Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);
        ChargingSession session = new ChargingSession(null, null, testCharger, startTime, 50.0);

        TelemetryData telemetry = simulator.computeTelemetry(session, testCharger, Instant.now());

        assertEquals(100.0, telemetry.getCurrentBatteryPct());
        assertEquals(0.0, telemetry.getCurrentPowerKw()); // Power drops to 0 kW at full charge
    }

    @Test
    @DisplayName("Should return persisted values for COMPLETED sessions")
    void testCompletedSessionValues() {
        Instant startTime = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant endTime = Instant.now().minus(5, ChronoUnit.MINUTES);

        ChargingSession session = new ChargingSession(null, null, testCharger, startTime, 20.0);
        session.setEndTime(endTime);
        session.setStatus(SessionStatus.COMPLETED);
        session.setCurrentBatteryPct(85.0);
        session.setEnergyConsumedKwh(BigDecimal.valueOf(48.750));
        session.setTotalCost(BigDecimal.valueOf(19.50));

        TelemetryData telemetry = simulator.computeTelemetry(session, testCharger, Instant.now());

        assertEquals(SessionStatus.COMPLETED, telemetry.getStatus());
        assertEquals(85.0, telemetry.getCurrentBatteryPct());
        assertEquals(BigDecimal.valueOf(48.750), telemetry.getEnergyDeliveredKwh());
        assertEquals(BigDecimal.valueOf(19.50), telemetry.getCurrentCost());
        assertEquals(0.0, telemetry.getCurrentPowerKw());
    }
}
