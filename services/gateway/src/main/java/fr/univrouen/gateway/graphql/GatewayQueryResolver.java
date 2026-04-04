package fr.univrouen.gateway.graphql;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import fr.univrouen.gateway.config.GatewayProperties;
import fr.univrouen.gateway.downstream.DownstreamGraphqlClient;
import fr.univrouen.gateway.graphql.model.DriverView;
import fr.univrouen.gateway.graphql.model.InterventionView;
import fr.univrouen.gateway.graphql.model.LicenseView;
import fr.univrouen.gateway.graphql.model.VehicleView;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GatewayQueryResolver {

    private static final String VEHICLES_QUERY = "query { vehicles { id vin dispo } }";
    private static final String VEHICLE_QUERY = "query($id: ID!) { vehicle(id: $id) { id vin dispo } }";

    private static final String DRIVERS_QUERY = "query { drivers { id prenom nom permis { id type date_valid } } }";
    private static final String DRIVER_QUERY = "query($id: ID!) { driver(id: $id) { id prenom nom permis { id type date_valid } } }";
    private static final String LICENSES_QUERY = "query { licenses { id type date_valid } }";
    private static final String LICENSE_QUERY = "query($id: ID!) { license(id: $id) { id type date_valid } }";

    private static final String INTERVENTIONS_QUERY = "query { interventions { id vehicle_id state date completed_date prix mileage_threshold current_mileage notes preventive_alert } }";
    private static final String INTERVENTION_QUERY = "query($id: ID!) { intervention(id: $id) { id vehicle_id state date completed_date prix mileage_threshold current_mileage notes preventive_alert } }";

    private final DownstreamGraphqlClient graphqlClient;
    private final GatewayProperties gatewayProperties;

    public GatewayQueryResolver(DownstreamGraphqlClient graphqlClient, GatewayProperties gatewayProperties) {
        this.graphqlClient = graphqlClient;
        this.gatewayProperties = gatewayProperties;
    }

    @QueryMapping
    public List<VehicleView> vehicles(@ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getVehiculesUrl(),
                VEHICLES_QUERY,
                Map.of(),
                authorization
        );
        return graphqlClient.toValue(data.path("vehicles"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public VehicleView vehicle(@Argument String id, @ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getVehiculesUrl(),
                VEHICLE_QUERY,
                Map.of("id", id),
                authorization
        );
        return graphqlClient.toValue(data.path("vehicle"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public List<DriverView> drivers(@ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getConducteursUrl(),
                DRIVERS_QUERY,
                Map.of(),
                authorization
        );
        return graphqlClient.toValue(data.path("drivers"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public DriverView driver(@Argument String id, @ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getConducteursUrl(),
                DRIVER_QUERY,
                Map.of("id", id),
                authorization
        );
        return graphqlClient.toValue(data.path("driver"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public List<LicenseView> licenses(@ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getConducteursUrl(),
                LICENSES_QUERY,
                Map.of(),
                authorization
        );
        return graphqlClient.toValue(data.path("licenses"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public LicenseView license(@Argument String id, @ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getConducteursUrl(),
                LICENSE_QUERY,
                Map.of("id", id),
                authorization
        );
        return graphqlClient.toValue(data.path("license"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public List<InterventionView> interventions(@ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getMaintenanceUrl(),
                INTERVENTIONS_QUERY,
                Map.of(),
                authorization
        );
        return graphqlClient.toValue(data.path("interventions"), new TypeReference<>() {
        });
    }

    @QueryMapping
    public InterventionView intervention(@Argument String id, @ContextValue("Authorization") String authorization) {
        JsonNode data = graphqlClient.queryData(
                gatewayProperties.getMaintenanceUrl(),
                INTERVENTION_QUERY,
                Map.of("id", id),
                authorization
        );
        return graphqlClient.toValue(data.path("intervention"), new TypeReference<>() {
        });
    }

    @SchemaMapping(typeName = "Vehicle", field = "interventions")
    public List<InterventionView> vehicleInterventions(
            VehicleView vehicle,
            DataFetchingEnvironment environment,
            @ContextValue("Authorization") String authorization
    ) {
        GraphQLContext graphQLContext = environment.getGraphQlContext();
        List<InterventionView> allInterventions = graphQLContext.get("interventions-cache");

        if (allInterventions == null) {
            allInterventions = interventions(authorization);
            graphQLContext.put("interventions-cache", allInterventions);
        }

        Integer vehicleId = vehicle.id();
        return allInterventions.stream()
                .filter(intervention -> vehicleId != null && vehicleId.equals(intervention.vehicle_id()))
                .toList();
    }
}
