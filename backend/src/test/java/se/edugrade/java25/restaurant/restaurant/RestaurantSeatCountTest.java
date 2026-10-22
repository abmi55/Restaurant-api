package se.edugrade.java25.restaurant.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import se.edugrade.java25.restaurant.booking.Booking;
import se.edugrade.java25.restaurant.booking.BookingRepository;
import se.edugrade.java25.restaurant.booking.NotificationService;
import se.edugrade.java25.restaurant.config.RestaurantAuthoritiesConverter;
import se.edugrade.java25.restaurant.security.TestTokenIssuer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantSeatCountTest {

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
        restaurant = restaurants.save(new Restaurant("Test", "Stockholm", "Swedish", 10));
    }

    @Test
    @DisplayName("Reducing seats below a future booking's party size returns 409")
    void futureBookingBlocksReduction() throws Exception {
        existing("2030-01-02T18:00", 8, false);
        update(6).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        assertThat(restaurants.findById(restaurant.getId()).orElseThrow().getSeatCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Past bookings do not block a seat reduction")
    void pastBookingDoesNotBlockReduction() throws Exception {
        existing("2029-12-31T18:00", 8, false);
        update(6).andExpect(status().isOk()).andExpect(jsonPath("$.seatCount").value(6));
    }

    @Test
    @DisplayName("Reducing seats below an ongoing booking's party size returns 409")
    void ongoingBookingBlocksReduction() throws Exception {
        existing("2030-01-01T11:00", 8, false);
        update(6).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Cancelled bookings do not block a seat reduction")
    void cancelledBookingDoesNotBlockReduction() throws Exception {
        existing("2030-01-02T18:00", 8, true);
        update(6).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Reducing seats to exactly fit existing bookings is accepted")
    void exactlyEnoughSeatsAreAccepted() throws Exception {
        existing("2030-01-02T18:00", 6, false);
        update(6).andExpect(status().isOk()).andExpect(jsonPath("$.seatCount").value(6));
    }

    @Test
    @DisplayName("An overbooked restaurant can keep or increase its seat count")
    void alreadyOverbookedRestaurantCanKeepOrRaiseSeatCount() throws Exception {
        existing("2030-01-02T18:00", 15, false);
        update(10).andExpect(status().isOk()).andExpect(jsonPath("$.seatCount").value(10));
        update(12).andExpect(status().isOk()).andExpect(jsonPath("$.seatCount").value(12));
    }

    private ResultActions update(int seatCount) throws Exception {
        return mvc.perform(put("/restaurants/{id}", restaurant.getId())
                .with(jwt().authorities(new RestaurantAuthoritiesConverter(TestTokenIssuer.ROLES_CLAIM))
                        .jwt(j -> j.subject("admin").claim("scope", "restaurants:write")
                                .claim(TestTokenIssuer.ROLES_CLAIM, List.of("ADMIN"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Updated", "city": "Stockholm", "cuisine": "Swedish", "seatCount": %d}
                        """.formatted(seatCount)));
    }

    private void existing(String dateTime, int partySize, boolean cancelled) {
        Booking booking = new Booking(restaurant, "Guest", "guest@example.test",
                LocalDateTime.parse(dateTime), partySize, null);
        if (cancelled) {
            booking.transitionTo(se.edugrade.java25.restaurant.booking.BookingStatus.CANCELLED);
        }
        bookings.save(booking);
    }
}
