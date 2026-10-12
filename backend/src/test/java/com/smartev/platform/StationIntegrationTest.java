package com.smartev.platform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class StationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should retrieve paginated list of stations")
    void testGetStations() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.totalElements", greaterThan(0)));
    }

    @Test
    @DisplayName("Should filter stations by search query")
    void testSearchStationsByQuery() throws Exception {
        mockMvc.perform(get("/api/v1/stations?query=Downtown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name", containsString("Downtown")));
    }

    @Test
    @DisplayName("Should get station by ID including its chargers")
    void testGetStationById() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.chargers", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should return 404 for non-existent station ID")
    void testGetStationNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/stations/9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should filter chargers by connector type and min power")
    void testFilterChargers() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1/chargers?connectorType=CCS2&minPower=100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].connectorType").value("CCS2"))
                .andExpect(jsonPath("$.data[0].maxPowerKw", greaterThanOrEqualTo(100.0)));
    }
}
