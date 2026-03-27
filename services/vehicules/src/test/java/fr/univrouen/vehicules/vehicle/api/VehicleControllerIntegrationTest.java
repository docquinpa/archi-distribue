package fr.univrouen.vehicules.vehicle.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@ActiveProfiles("test")
class VehicleControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldPerformCrudLifecycle() throws Exception {
        String createBody = mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"VIN-123\",\"dispo\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vin").value("VIN-123"))
                .andExpect(jsonPath("$.dispo").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Pattern pattern = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(createBody);
        matcher.find();
        int id = Integer.parseInt(matcher.group(1));

        mockMvc.perform(get("/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("VIN-123"))
                .andExpect(jsonPath("$.dispo").value(true));

        mockMvc.perform(put("/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"VIN-123-UPDATED\",\"dispo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.vin").value("VIN-123-UPDATED"))
                .andExpect(jsonPath("$.dispo").value(false));

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/vehicles/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/vehicles/{id}", id))
                .andExpect(status().isNotFound());
    }
}
