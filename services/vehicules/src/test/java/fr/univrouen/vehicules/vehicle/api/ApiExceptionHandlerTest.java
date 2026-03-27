package fr.univrouen.vehicules.vehicle.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapNotFoundTo404() {
        var response = handler.handleNotFound(new VehicleNotFoundException(42));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("message", "Vehicle not found: 42");
    }

    @Test
    void shouldMapConstraintViolationTo409() {
        var response = handler.handleConflict(new DataIntegrityViolationException("dup"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).containsKey("message");
    }
}
