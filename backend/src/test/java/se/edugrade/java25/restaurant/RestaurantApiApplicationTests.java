package se.edugrade.java25.restaurant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starts the whole application context. It fails if a bean cannot be created,
 * which catches most wiring mistakes before you ever open a browser.
 *
 * <p>Your own tests go in this same source tree. Part 2 of the assignment
 * replaces this one as the interesting test.
 */
@SpringBootTest
class RestaurantApiApplicationTests {

    @Test
    @DisplayName("Application context loads")
    void contextLoads() {
    }
}
