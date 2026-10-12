package com.smartev.platform.dashboard;

import com.smartev.platform.charging.SessionResponse;
import com.smartev.platform.reservation.ReservationResponse;

import java.math.BigDecimal;
import java.util.List;

public class DashboardMetricsDTO {

    private long availableChargers;
    private long totalStations;
    private long activeReservations;
    private long activeChargingSessions;
    private BigDecimal totalEnergyDeliveredKwh;
    private BigDecimal userTotalCost;
    private SessionResponse activeSession;
    private List<SessionResponse> recentSessions;
    private List<ReservationResponse> recentReservations;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(long availableChargers,
                               long totalStations,
                               long activeReservations,
                               long activeChargingSessions,
                               BigDecimal totalEnergyDeliveredKwh,
                               BigDecimal userTotalCost,
                               SessionResponse activeSession,
                               List<SessionResponse> recentSessions,
                               List<ReservationResponse> recentReservations) {
        this.availableChargers = availableChargers;
        this.totalStations = totalStations;
        this.activeReservations = activeReservations;
        this.activeChargingSessions = activeChargingSessions;
        this.totalEnergyDeliveredKwh = totalEnergyDeliveredKwh;
        this.userTotalCost = userTotalCost;
        this.activeSession = activeSession;
        this.recentSessions = recentSessions;
        this.recentReservations = recentReservations;
    }

    public long getAvailableChargers() {
        return availableChargers;
    }

    public void setAvailableChargers(long availableChargers) {
        this.availableChargers = availableChargers;
    }

    public long getTotalStations() {
        return totalStations;
    }

    public void setTotalStations(long totalStations) {
        this.totalStations = totalStations;
    }

    public long getActiveReservations() {
        return activeReservations;
    }

    public void setActiveReservations(long activeReservations) {
        this.activeReservations = activeReservations;
    }

    public long getActiveChargingSessions() {
        return activeChargingSessions;
    }

    public void setActiveChargingSessions(long activeChargingSessions) {
        this.activeChargingSessions = activeChargingSessions;
    }

    public BigDecimal getTotalEnergyDeliveredKwh() {
        return totalEnergyDeliveredKwh;
    }

    public void setTotalEnergyDeliveredKwh(BigDecimal totalEnergyDeliveredKwh) {
        this.totalEnergyDeliveredKwh = totalEnergyDeliveredKwh;
    }

    public BigDecimal getUserTotalCost() {
        return userTotalCost;
    }

    public void setUserTotalCost(BigDecimal userTotalCost) {
        this.userTotalCost = userTotalCost;
    }

    public SessionResponse getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(SessionResponse activeSession) {
        this.activeSession = activeSession;
    }

    public List<SessionResponse> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<SessionResponse> recentSessions) {
        this.recentSessions = recentSessions;
    }

    public List<ReservationResponse> getRecentReservations() {
        return recentReservations;
    }

    public void setRecentReservations(List<ReservationResponse> recentReservations) {
        this.recentReservations = recentReservations;
    }
}
