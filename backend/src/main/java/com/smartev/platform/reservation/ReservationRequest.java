package com.smartev.platform.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReservationRequest {

    @NotNull(message = "Charger ID is required")
    private Long chargerId;

    @Min(value = 10, message = "Reservation duration must be at least 10 minutes")
    @Max(value = 180, message = "Reservation duration cannot exceed 180 minutes")
    private Integer durationMinutes = 30;

    private String idempotencyKey;

    public ReservationRequest() {}

    public ReservationRequest(Long chargerId, Integer durationMinutes, String idempotencyKey) {
        this.chargerId = chargerId;
        this.durationMinutes = durationMinutes != null ? durationMinutes : 30;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getChargerId() {
        return chargerId;
    }

    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
