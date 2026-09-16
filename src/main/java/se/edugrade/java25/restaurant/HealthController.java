package se.edugrade.java25.restaurant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proves that the application started and can answer an HTTP request.
 *
 * <p>This is not part of the assignment. Keep it as a deployment smoke test
 * (it is handy to curl after a deploy) or delete it once your own endpoints
 * exist.
 *
 * <p>Note: this is not Spring Boot Actuator. If you later add
 * spring-boot-starter-actuator you get a different, richer endpoint at
 * /actuator/health, and the two can coexist.
 */
@RestController
public class HealthController {

    public record Health(String status, String service) {}

    @GetMapping("/health")
    public Health health() {
        return new Health("UP", "restaurant-api");
    }
}
