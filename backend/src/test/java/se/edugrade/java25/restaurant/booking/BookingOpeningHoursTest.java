package se.edugrade.java25.restaurant.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import se.edugrade.java25.restaurant.config.RestaurantAuthoritiesConverter;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;
import se.edugrade.java25.restaurant.security.TestTokenIssuer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingOpeningHoursTest {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookings;
    @Autowired RestaurantRepository restaurants;
    @MockitoBean NotificationService notifications;
    @TestBean Clock clock;

    private Restaurant restaurant;

    static Clock clock() {
        ZoneId zone = ZoneId.of("Europe/Stockholm");
        return Clock.fixed(ZonedDateTime.of(2030, 1, 1, 12, 0, 0, 0, zone).toInstant(), zone);
    }

    @BeforeEach
    void setUp() {
        bookings.deleteAll();
        restaurants.deleteAll();
        restaurant = restaurants.save(new Restaurant("Test", "Stockholm", "Swedish", 10,
                LocalTime.of(11, 0), LocalTime.of(23, 0), LocalTime.of(12, 0), LocalTime.of(22, 0)));
    }

    @ParameterizedTest
    @DisplayName("Bookings outside weekday or weekend hours return 400 with opening hours and last start time")
    @CsvSource({
            "2030-01-02T10:59, weekdays, 11:00–23:00, 21:00",
            "2030-01-02T21:01, weekdays, 11:00–23:00, 21:00",
            "2030-01-02T23:00, weekdays, 11:00–23:00, 21:00",
            "2030-01-05T11:00, weekends, 12:00–22:00, 20:00",
            "2030-01-06T20:01, weekends, 12:00–22:00, 20:00"
    })
    void outsideOpeningHoursReturnsProblemDetail(String dateTime, String dayType,
                                                String hours, String lastStart) throws Exception {
        create(restaurant.getId(), dateTime, 2)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail", containsString(dayType)))
                .andExpect(jsonPath("$.detail", containsString(hours)))
                .andExpect(jsonPath("$.detail", containsString(lastStart)));
        assertThat(bookings.count()).isZero();
    }

    @ParameterizedTest
    @DisplayName("Bookings at opening and the last start time are accepted on weekdays and weekends")
    @CsvSource({
            "2030-01-02T11:00", "2030-01-02T21:00",
            "2030-01-05T12:00", "2030-01-05T20:00"
    })
    void boundariesAndDayTypesAreAccepted(String dateTime) throws Exception {
        create(restaurant.getId(), dateTime, 2).andExpect(status().isCreated());
        assertThat(bookings.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("An unknown restaurant returns 404 before opening hours are checked")
    void unknownRestaurantTakesPrecedenceOverOpeningHours() throws Exception {
        create(Long.MAX_VALUE, "2030-01-02T10:59", 2)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Opening hours are checked before capacity and insufficient capacity still returns 409")
    void openingHoursTakePrecedenceOverInsufficientCapacity() throws Exception {
        bookings.save(new Booking(restaurant, "Existing", "existing@example.test",
                LocalDateTime.parse("2030-01-02T20:00"), 10, "guest"));

        create(restaurant.getId(), "2030-01-02T21:01", 2)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("11:00–23:00")));
        create(restaurant.getId(), "2030-01-02T21:00", 2).andExpect(status().isConflict());
        assertThat(bookings.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Confirming and seating an existing booking do not recheck opening hours")
    void confirmationAndSeatingDoNotRecheckOpeningHours() throws Exception {
        Booking booking = bookings.save(new Booking(restaurant, "Existing", "existing@example.test",
                LocalDateTime.parse("2030-01-02T03:00"), 2, "guest"));

        mvc.perform(post("/bookings/{id}/confirm", booking.getId()).with(token("STAFF", "bookings:manage")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(post("/bookings/{id}/seat", booking.getId()).with(token("STAFF", "bookings:manage")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SEATED"));
    }

    private ResultActions create(Long restaurantId, String dateTime, int partySize) throws Exception {
        return mvc.perform(post("/bookings")
                .with(token("GUEST", "bookings:write"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"restaurantId": %d, "guestName": "Guest", "guestEmail": "guest@example.test",
                         "dateTime": "%s", "partySize": %d}
                        """.formatted(restaurantId, dateTime, partySize)));
    }

    private JwtRequestPostProcessor token(String role, String scope) {
        return jwt().authorities(new RestaurantAuthoritiesConverter(TestTokenIssuer.ROLES_CLAIM))
                .jwt(j -> j.subject("guest").claim("scope", scope)
                        .claim(TestTokenIssuer.ROLES_CLAIM, List.of(role)));
    }
}
