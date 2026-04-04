package fr.univrouen.gateway.downstream;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamGraphqlClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DownstreamGraphqlClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public JsonNode queryData(String endpoint, String query, Map<String, Object> variables, String authorizationHeader) {
        GraphqlRequest request = new GraphqlRequest(query, variables == null ? Map.of() : variables);

        GraphqlResponse response = restClient
                .post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                    }
                })
                .body(request)
                .retrieve()
                .body(GraphqlResponse.class);

        if (response == null) {
            throw new IllegalStateException("Downstream GraphQL returned an empty response");
        }

        if (response.errors() != null && !response.errors().isEmpty()) {
            String message = response.errors().getFirst().path("message").asText("unknown downstream error");
            throw new IllegalStateException("Downstream GraphQL error: " + message);
        }

        return response.data() == null ? objectMapper.createObjectNode() : response.data();
    }

    public <T> T toValue(JsonNode node, TypeReference<T> type) {
        return objectMapper.convertValue(node, type);
    }

    private record GraphqlRequest(String query, Map<String, Object> variables) {
    }

    private record GraphqlResponse(JsonNode data, List<JsonNode> errors) {
    }
}
