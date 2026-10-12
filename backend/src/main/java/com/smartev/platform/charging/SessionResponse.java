package com.smartev.platform.charging;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.charger.ConnectorType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public class SessionResponse {

    private Long id;
    private Long reservationId;
    private Long userId;
    private String userName;
    private Long chargerId;
    private Long stationId;
    private String stationName;
    private String stationAddress;
    private ConnectorType connectorType;
    private Double maxPowerKw;
    private BigDecimal pricePerKwh;
    private Instant startTime;
    private Instant endTime;
    private SessionStatus status;
    private BigDecimal energyConsumedKwh;
    private Double startBatteryPct;
    private Double currentBatteryPct;
    private BigDecimal totalCost;
    private long elapsedSeconds;

    public SessionResponse() {}

    public SessionResponse(ChargingSession session) {
        this.id = session.getId();
        if (session.getReservation() != null) {
            this.reservationId = session.getReservation().getId();
        }
        if (session.getUser() != null) {
            this.userId = session.getUser().getId();
            this.userName = session.getUser().getName();
        }
        Charger charger = session.getCharger();
        if (charger != null) {
            this.chargerId = charger.getId();
            this.connectorType = charger.getConnectorType();
            this.maxPowerKw = charger.getMaxPowerKw();
            this.pricePerKwh = charger.getPricePerKwh();
            if (charger.getStation() != null) {
                this.stationId = charger.getStation().getId();
                this.stationName = charger.getStation().getName();
                this.stationAddress = charger.getStation().getAddress();
            }
        }
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.status = session.getStatus();
        this.energyConsumedKwh = session.getEnergyConsumedKwh();
        this.startBatteryPct = session.getStartBatteryPct();
        this.currentBatteryPct = session.getCurrentBatteryPct();
        this.totalCost = session.getTotalCost();

        Instant refEnd = session.getEndTime() != null ? session.getEndTime() : Instant.now();
        this.elapsedSeconds = Math.max(0, Duration.between(session.getStartTime(), refEnd).getSeconds());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getChargerId() {
        return chargerId;
    }

    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationAddress() {
        return stationAddress;
    }

    public void setStationAddress(String stationAddress) {
        this.stationAddress = stationAddress;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public Double getMaxPowerKw() {
        return maxPowerKw;
    }

    public void setMaxPowerKw(Double maxPowerKw) {
        this.maxPowerKw = maxPowerKw;
    }

    public BigDecimal getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(BigDecimal pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public BigDecimal getEnergyConsumedKwh() {
        return energyConsumedKwh;
    }

    public void setEnergyConsumedKwh(BigDecimal energyConsumedKwh) {
        this.energyConsumedKwh = energyConsumedKwh;
    }

    public Double getStartBatteryPct() {
        return startBatteryPct;
    }

    public void setStartBatteryPct(Double startBatteryPct) {
        this.startBatteryPct = startBatteryPct;
    }

    public Double getCurrentBatteryPct() {
        return currentBatteryPct;
    }

    public void setCurrentBatteryPct(Double currentBatteryPct) {
        this.currentBatteryPct = currentBatteryPct;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }
}
