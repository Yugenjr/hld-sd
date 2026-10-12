package com.smartev.platform.station;

import com.smartev.platform.charger.ChargerResponse;
import com.smartev.platform.charger.ChargerStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StationResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private StationStatus operationalStatus;
    private String amenities;
    private int totalChargers;
    private int availableChargers;
    private List<ChargerResponse> chargers;

    public StationResponse() {}

    public StationResponse(ChargingStation station, boolean includeChargers) {
        this.id = station.getId();
        this.name = station.getName();
        this.address = station.getAddress();
        this.latitude = station.getLatitude();
        this.longitude = station.getLongitude();
        this.operationalStatus = station.getOperationalStatus();
        this.amenities = station.getAmenities();
        if (station.getChargers() != null) {
            this.totalChargers = station.getChargers().size();
            this.availableChargers = (int) station.getChargers().stream()
                    .filter(c -> c.getStatus() == ChargerStatus.AVAILABLE)
                    .count();
            if (includeChargers) {
                this.chargers = station.getChargers().stream()
                        .map(ChargerResponse::new)
                        .collect(Collectors.toList());
            } else {
                this.chargers = Collections.emptyList();
            }
        } else {
            this.totalChargers = 0;
            this.availableChargers = 0;
            this.chargers = Collections.emptyList();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public StationStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(StationStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public int getTotalChargers() {
        return totalChargers;
    }

    public void setTotalChargers(int totalChargers) {
        this.totalChargers = totalChargers;
    }

    public int getAvailableChargers() {
        return availableChargers;
    }

    public void setAvailableChargers(int availableChargers) {
        this.availableChargers = availableChargers;
    }

    public List<ChargerResponse> getChargers() {
        return chargers;
    }

    public void setChargers(List<ChargerResponse> chargers) {
        this.chargers = chargers;
    }
}
