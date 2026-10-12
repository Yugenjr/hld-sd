package com.smartev.platform.charger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartev.platform.station.ChargingStation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "chargers")
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @JsonIgnore
    private ChargingStation station;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false, length = 50)
    private ConnectorType connectorType;

    @NotNull
    @Column(name = "max_power_kw", nullable = false)
    private Double maxPowerKw;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    @NotNull
    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Charger() {}

    public Charger(ChargingStation station, ConnectorType connectorType, Double maxPowerKw, ChargerStatus status, BigDecimal pricePerKwh) {
        this.station = station;
        this.connectorType = connectorType;
        this.maxPowerKw = maxPowerKw;
        this.status = status != null ? status : ChargerStatus.AVAILABLE;
        this.pricePerKwh = pricePerKwh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChargingStation getStation() {
        return station;
    }

    public void setStation(ChargingStation station) {
        this.station = station;
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

    public ChargerStatus getStatus() {
        return status;
    }

    public void setStatus(ChargerStatus status) {
        this.status = status;
    }

    public BigDecimal getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(BigDecimal pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
