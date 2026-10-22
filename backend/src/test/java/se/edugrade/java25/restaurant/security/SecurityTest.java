package se.edugrade.java25.restaurant.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.java25.restaurant.booking.Booking;
import se.edugrade.java25.restaurant.booking.BookingRepository;
import se.edugrade.java25.restaurant.booking.NotificationService;
import se.edugrade.java25.restaurant.config.RestaurantAuthoritiesConverter;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint rules, CSRF and CORS tested through MockMvc with
 * {@code SecurityMockMvcRequestPostProcessors.jwt()}.
 *
 * <p>{@code jwt()} skips the decoder, so signature, issuer and audience are not
 * checked here; {@code BookingApiTest} sends signed tokens through a real decoder. What runs
 * for real is the filter chain and the application's
 * {@link RestaurantAuthoritiesConverter}, which receives claims shaped like an
 * Auth0 access token.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    private static final String ROLES_CLAIM = TestTokenIssuer.ROLES_CLAIM;
    private static final String ALLOWED_ORIGIN = "http://localhost:5173";
    private static final String UNKNOWN_ORIGIN = "https://evil.example";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Long restaurantId;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        restaurantRepository.deleteAll();
        Restaurant restaurant = restaurantRepository.save(new Restaurant("Test Restaurant", "Stockholm", "Swedish", 20));
        restaurantId = restaurant.getId();
        bookingId = bookingRepository.save(new Booking(restaurant, "Test Guest", "guest@example.test",
                LocalDateTime.of(2030, 1, 1, 18, 0), 2, "user")).getId();
    }

    @Test
    @DisplayName("POST /restaurants without a token returns 401")
    void createRestaurantWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restaurantJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")));
    }

    @Test
    @DisplayName("POST /restaurants as ADMIN with restaurants:write returns 201")
    void createRestaurantAsAdminReturns201() throws Exception {
        int status = mockMvc.perform(post("/restaurants")
                        .with(token("restaurants:write", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restaurantJson()))
                .andReturn().getResponse().getStatus();

        assertThat(status)
                .withFailMessage("Expected 201 but got %d. 403 with the right role and scope usually means "
                        + "the rule uses hasRole for a scope, or the converter replaces SCOPE_* instead of "
                        + "adding ROLE_*.", status)
                .isEqualTo(201);
    }

    @Test
    @DisplayName("POST /restaurants as GUEST returns 403")
    void createRestaurantAsGuestReturns403() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .with(token("bookings:write", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restaurantJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /bookings/{id}/confirm as STAFF with bookings:manage returns 200")
    void confirmBookingAsStaffReturns200() throws Exception {
        int status = mockMvc.perform(post("/bookings/{id}/confirm", bookingId)
                        .with(token("bookings:manage", "STAFF")))
                .andReturn().getResponse().getStatus();

        assertThat(status)
                .withFailMessage("Expected 200 but got %d. 403 usually means the roles claim is not mapped "
                        + "to ROLE_STAFF or bookings:manage is not mapped to SCOPE_bookings:manage.", status)
                .isEqualTo(200);
    }

    @Test
    @DisplayName("POST /bookings/{id}/confirm as GUEST returns 403")
    void confirmBookingAsGuestReturns403() throws Exception {
        mockMvc.perform(post("/bookings/{id}/confirm", bookingId)
                        .with(token("bookings:write", "GUEST")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /bookings/{id}/confirm as STAFF without bookings:manage returns 403")
    void confirmBookingAsStaffWithoutScopeReturns403() throws Exception {
        int status = mockMvc.perform(post("/bookings/{id}/confirm", bookingId)
                        .with(token("", "STAFF")))
                .andReturn().getResponse().getStatus();

        assertThat(status)
                .withFailMessage("Expected 403 but got %d. 200 means the rule checks only the role and "
                        + "ignores the bookings:manage scope.", status)
                .isEqualTo(403);
    }

    @Test
    @DisplayName("POST /bookings with a bearer token and no CSRF token returns 201")
    void postWithBearerTokenAndNoCsrfTokenSucceeds() throws Exception {
        mockMvc.perform(post("/bookings")
                        .with(token("bookings:write", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("A preflight from the allowed origin is accepted without a token")
    void preflightFromAllowedOriginIsAcceptedWithoutToken() throws Exception {
        mockMvc.perform(options("/bookings")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    @DisplayName("A preflight from an unknown origin is rejected")
    void preflightFromUnknownOriginIsRejected() throws Exception {
        mockMvc.perform(options("/bookings")
                        .header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // Claims shaped like an Auth0 access token, mapped by the application's own converter.
    private JwtRequestPostProcessor token(String scope, String... roles) {
        return jwt()
                .authorities(new RestaurantAuthoritiesConverter(ROLES_CLAIM))
                .jwt(j -> j.claim("scope", scope).claim(ROLES_CLAIM, List.of(roles)));
    }

    private String restaurantJson() {
        return """
                {
                  "name": "New Restaurant",
                  "city": "Göteborg",
                  "cuisine": "Italian",
                  "seatCount": 30
                }
                """;
    }

    private String bookingJson() {
        return """
                {
                  "restaurantId": %d,
                  "guestName": "Test Guest",
                  "guestEmail": "guest@example.test",
                  "dateTime": "2030-01-01T18:00:00",
                  "partySize": 2
                }
                """.formatted(restaurantId);
    }
}
