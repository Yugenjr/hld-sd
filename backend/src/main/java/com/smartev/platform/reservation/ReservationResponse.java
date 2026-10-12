package com.smartev.platform.reservation;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.charger.ConnectorType;

import java.math.BigDecimal;
import java.time.Instant;

public class ReservationResponse {

    private Long id;
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
    private Instant expiryTime;
    private ReservationStatus status;
    private String idempotencyKey;
    private Instant createdAt;

    public ReservationResponse() {}

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        if (reservation.getUser() != null) {
            this.userId = reservation.getUser().getId();
            this.userName = reservation.getUser().getName();
        }
        Charger charger = reservation.getCharger();
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
        this.startTime = reservation.getStartTime();
        this.expiryTime = reservation.getExpiryTime();
        this.status = reservation.getStatus();
        this.idempotencyKey = reservation.getIdempotencyKey();
        this.createdAt = reservation.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
