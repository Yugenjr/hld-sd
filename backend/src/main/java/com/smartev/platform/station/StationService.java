package com.smartev.platform.station;

import com.smartev.platform.charger.*;
import com.smartev.platform.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;

    public StationService(StationRepository stationRepository, ChargerRepository chargerRepository) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
    }

    public Page<StationResponse> getStations(String query, StationStatus status, Pageable pageable) {
        String sanitizedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Page<ChargingStation> stations = stationRepository.findWithFilters(sanitizedQuery, status, pageable);
        return stations.map(station -> new StationResponse(station, false));
    }

    public StationResponse getStationById(Long id) {
        ChargingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with id: " + id));
        return new StationResponse(station, true);
    }

    public List<ChargerResponse> getStationChargers(Long stationId, ConnectorType connectorType, ChargerStatus status, Double minPower) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Charging station not found with id: " + stationId);
        }
        List<Charger> chargers = chargerRepository.findByStationWithFilters(stationId, connectorType, status, minPower);
        return chargers.stream().map(ChargerResponse::new).collect(Collectors.toList());
    }

    public ChargerResponse getChargerById(Long chargerId) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with id: " + chargerId));
        return new ChargerResponse(charger);
    }
}
