package com.smartev.platform.charger;

import java.math.BigDecimal;

public class ChargerResponse {

    private Long id;
    private Long stationId;
    private String stationName;
    private ConnectorType connectorType;
    private Double maxPowerKw;
    private ChargerStatus status;
    private BigDecimal pricePerKwh;

    public ChargerResponse() {}

    public ChargerResponse(Charger charger) {
        this.id = charger.getId();
        if (charger.getStation() != null) {
            this.stationId = charger.getStation().getId();
            this.stationName = charger.getStation().getName();
        }
        this.connectorType = charger.getConnectorType();
        this.maxPowerKw = charger.getMaxPowerKw();
        this.status = charger.getStatus();
        this.pricePerKwh = charger.getPricePerKwh();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
