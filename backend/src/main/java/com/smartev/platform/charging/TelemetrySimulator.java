package com.smartev.platform.charging;

import com.smartev.platform.charger.Charger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class TelemetrySimulator {

    public static final double BATTERY_CAPACITY_KWH = 75.0; // Standard 75 kWh EV pack (e.g. Model 3 / Ioniq 5)
    private static final double SIMULATION_TIME_WARP = 15.0; // 15x simulation speed for smooth live browser demo

    public TelemetryData computeTelemetry(ChargingSession session, Charger charger, Instant now) {
        Instant startTime = session.getStartTime();
        Instant referenceEnd = session.getEndTime() != null ? session.getEndTime() : now;

        long elapsedSeconds = Math.max(0, Duration.between(startTime, referenceEnd).getSeconds());

        if (session.getStatus() == SessionStatus.COMPLETED) {
            return new TelemetryData(
                    session.getId(),
                    session.getStatus(),
                    elapsedSeconds,
                    session.getCurrentBatteryPct(),
                    session.getEnergyConsumedKwh(),
                    session.getTotalCost(),
                    0.0
            );
        }

        double maxPower = charger != null ? charger.getMaxPowerKw() : 150.0;
        BigDecimal pricePerKwh = charger != null ? charger.getPricePerKwh() : BigDecimal.valueOf(0.35);

        double startBatteryPct = session.getStartBatteryPct();
        double remainingCapacityPct = Math.max(0.0, 100.0 - startBatteryPct);
        double maxEnergyDeliverable = (BATTERY_CAPACITY_KWH * remainingCapacityPct) / 100.0;

        // Virtual seconds elapsed
        double virtualHours = (elapsedSeconds * SIMULATION_TIME_WARP) / 3600.0;

        // Taper charging curve as SoC rises above 80%
        double currentSocEstimate = startBatteryPct + (virtualHours * maxPower / BATTERY_CAPACITY_KWH * 100.0);
        double powerMultiplier = currentSocEstimate > 80.0 ? 0.45 : 0.95;
        double effectivePower = maxPower * powerMultiplier;

        double energyDelivered = Math.min(maxEnergyDeliverable, virtualHours * effectivePower);
        double currentBatteryPct = Math.min(100.0, startBatteryPct + (energyDelivered / BATTERY_CAPACITY_KWH * 100.0));

        BigDecimal energyKwh = BigDecimal.valueOf(energyDelivered).setScale(3, RoundingMode.HALF_UP);
        BigDecimal cost = energyKwh.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);

        return new TelemetryData(
                session.getId(),
                session.getStatus(),
                elapsedSeconds,
                Math.round(currentBatteryPct * 10.0) / 10.0,
                energyKwh,
                cost,
                currentBatteryPct >= 100.0 ? 0.0 : Math.round(effectivePower * 10.0) / 10.0
        );
    }
}
