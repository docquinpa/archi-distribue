package fr.univrouen.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.downstream")
public class GatewayProperties {

    private String vehiculesUrl;
    private String conducteursUrl;
    private String maintenanceUrl;

    public String getVehiculesUrl() {
        return vehiculesUrl;
    }

    public void setVehiculesUrl(String vehiculesUrl) {
        this.vehiculesUrl = vehiculesUrl;
    }

    public String getConducteursUrl() {
        return conducteursUrl;
    }

    public void setConducteursUrl(String conducteursUrl) {
        this.conducteursUrl = conducteursUrl;
    }

    public String getMaintenanceUrl() {
        return maintenanceUrl;
    }

    public void setMaintenanceUrl(String maintenanceUrl) {
        this.maintenanceUrl = maintenanceUrl;
    }
}
