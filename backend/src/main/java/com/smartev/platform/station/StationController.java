package com.smartev.platform.station;

import com.smartev.platform.charger.ChargerResponse;
import com.smartev.platform.charger.ChargerStatus;
import com.smartev.platform.charger.ConnectorType;
import com.smartev.platform.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "Charging Stations", description = "Endpoints for discovering charging stations and their chargers")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    @Operation(summary = "Search and list charging stations with filtering and pagination")
    public ResponseEntity<ApiResponse<Page<StationResponse>>> getStations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) StationStatus status,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<StationResponse> stations = stationService.getStations(query, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Stations retrieved successfully", stations));
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Get station details including its chargers")
    public ResponseEntity<ApiResponse<StationResponse>> getStationById(@PathVariable Long stationId) {
        StationResponse station = stationService.getStationById(stationId);
        return ResponseEntity.ok(ApiResponse.ok("Station details retrieved successfully", station));
    }

    @GetMapping("/{stationId}/chargers")
    @Operation(summary = "Get chargers for a specific station with connector and power filters")
    public ResponseEntity<ApiResponse<List<ChargerResponse>>> getStationChargers(
            @PathVariable Long stationId,
            @RequestParam(required = false) ConnectorType connectorType,
            @RequestParam(required = false) ChargerStatus status,
            @RequestParam(required = false) Double minPower) {
        List<ChargerResponse> chargers = stationService.getStationChargers(stationId, connectorType, status, minPower);
        return ResponseEntity.ok(ApiResponse.ok("Chargers retrieved successfully", chargers));
    }
}
