package fr.univrouen.driver.conducteur.graphql;

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
class ConducteurGraphqlIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldCreateAndQueryDriverThroughGraphql() throws Exception {
        String createLicenseMutation = "mutation { createLicense(type:\"B\", date_valid:\"2026-12-31\") { id } }";
        String licenseResponse = mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", createLicenseMutation))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createLicense.id").exists())
                .andReturn()
                .getResponse().getContentAsString();

        int permisId = objectMapper.readTree(licenseResponse).at("/data/createLicense/id").asInt();

        String createDriverMutation = String.format("mutation { createDriver(prenom:\"Jean\", nom:\"Dupont\", permisId:%d) { id prenom nom } }", permisId);
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", createDriverMutation))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createDriver.prenom").value("Jean"));

        String query = "{ drivers { prenom nom } }";
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", query))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.drivers[0].prenom").value("Jean"));
    }
}
