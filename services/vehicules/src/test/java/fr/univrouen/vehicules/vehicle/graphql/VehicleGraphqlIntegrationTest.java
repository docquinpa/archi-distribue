package fr.univrouen.vehicules.vehicle.graphql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class VehicleGraphqlIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldCreateAndQueryVehicleThroughGraphql() throws Exception {
        String createMutation = "mutation { createVehicle(vin:\"VIN-GQL-1\", dispo:true) { id vin dispo } }";
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("query", createMutation))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createVehicle.vin").value("VIN-GQL-1"))
                .andExpect(jsonPath("$.data.createVehicle.dispo").value(true));

        String query = "{ vehicles { vin dispo } }";
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("query", query))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicles[0].vin").value("VIN-GQL-1"));
    }
}
