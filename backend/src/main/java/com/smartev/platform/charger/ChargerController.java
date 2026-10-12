package com.smartev.platform.charger;

import com.smartev.platform.common.ApiResponse;
import com.smartev.platform.station.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chargers")
@Tag(name = "Chargers", description = "Endpoints for inspecting individual EV chargers")
public class ChargerController {

    private final StationService stationService;

    public ChargerController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/{chargerId}")
    @Operation(summary = "Get single charger status and details")
    public ResponseEntity<ApiResponse<ChargerResponse>> getChargerById(@PathVariable Long chargerId) {
        ChargerResponse charger = stationService.getChargerById(chargerId);
        return ResponseEntity.ok(ApiResponse.ok("Charger details retrieved successfully", charger));
    }
}
