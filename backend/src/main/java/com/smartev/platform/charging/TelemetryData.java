package com.smartev.platform.charging;

import java.math.BigDecimal;

public class TelemetryData {

    private Long sessionId;
    private SessionStatus status;
    private long elapsedSeconds;
    private Double currentBatteryPct;
    private BigDecimal energyDeliveredKwh;
    private BigDecimal currentCost;
    private Double currentPowerKw;
    private boolean simulated = true;

    public TelemetryData() {}

    public TelemetryData(Long sessionId, SessionStatus status, long elapsedSeconds, Double currentBatteryPct,
                         BigDecimal energyDeliveredKwh, BigDecimal currentCost, Double currentPowerKw) {
        this.sessionId = sessionId;
        this.status = status;
        this.elapsedSeconds = elapsedSeconds;
        this.currentBatteryPct = currentBatteryPct;
        this.energyDeliveredKwh = energyDeliveredKwh;
        this.currentCost = currentCost;
        this.currentPowerKw = currentPowerKw;
        this.simulated = true;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public Double getCurrentBatteryPct() {
        return currentBatteryPct;
    }

    public void setCurrentBatteryPct(Double currentBatteryPct) {
        this.currentBatteryPct = currentBatteryPct;
    }

    public BigDecimal getEnergyDeliveredKwh() {
        return energyDeliveredKwh;
    }

    public void setEnergyDeliveredKwh(BigDecimal energyDeliveredKwh) {
        this.energyDeliveredKwh = energyDeliveredKwh;
    }

    public BigDecimal getCurrentCost() {
        return currentCost;
    }

    public void setCurrentCost(BigDecimal currentCost) {
        this.currentCost = currentCost;
    }

    public Double getCurrentPowerKw() {
        return currentPowerKw;
    }

    public void setCurrentPowerKw(Double currentPowerKw) {
        this.currentPowerKw = currentPowerKw;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public void setSimulated(boolean simulated) {
        this.simulated = simulated;
    }
}
