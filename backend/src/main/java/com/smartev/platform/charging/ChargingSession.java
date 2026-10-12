package com.smartev.platform.charging;

import com.smartev.platform.charger.Charger;
import com.smartev.platform.reservation.Reservation;
import com.smartev.platform.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "charging_sessions")
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionStatus status = SessionStatus.CHARGING;

    @NotNull
    @Column(name = "energy_consumed_kwh", nullable = false, precision = 10, scale = 3)
    private BigDecimal energyConsumedKwh = BigDecimal.ZERO;

    @NotNull
    @Column(name = "start_battery_pct", nullable = false)
    private Double startBatteryPct = 20.0;

    @NotNull
    @Column(name = "current_battery_pct", nullable = false)
    private Double currentBatteryPct = 20.0;

    @NotNull
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public ChargingSession() {}

    public ChargingSession(Reservation reservation, User user, Charger charger, Instant startTime, Double startBatteryPct) {
        this.reservation = reservation;
        this.user = user;
        this.charger = charger;
        this.startTime = startTime;
        this.startBatteryPct = startBatteryPct != null ? startBatteryPct : 20.0;
        this.currentBatteryPct = this.startBatteryPct;
        this.status = SessionStatus.CHARGING;
        this.energyConsumedKwh = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Charger getCharger() {
        return charger;
    }

    public void setCharger(Charger charger) {
        this.charger = charger;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
