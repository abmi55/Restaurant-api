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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Long restaurantId;
    private Long guestABookingId;
    private Long guestBBookingId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        restaurantRepository.deleteAll();
        Restaurant restaurant = restaurantRepository.save(new Restaurant("Test Restaurant", "Stockholm", "Swedish", 20));
        restaurantId = restaurant.getId();
        guestABookingId = bookingRepository.save(new Booking(restaurant, "Guest A", "guest-a@example.test",
                LocalDateTime.of(2030, 1, 1, 18, 0), 2, "guest-a")).getId();
        guestBBookingId = bookingRepository.save(new Booking(restaurant, "Guest B", "guest-b@example.test",
                LocalDateTime.of(2030, 1, 1, 19, 0), 2, "guest-b")).getId();
    }

    @Test
    @DisplayName("GET /bookings as GUEST returns only the guest's own bookings")
    void guestListsOnlyOwnBookings() throws Exception {
        mockMvc.perform(get("/bookings").with(token("guest-a", "GUEST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(guestABookingId));
    }

    @Test
    @DisplayName("GET /bookings/{id} for another guest's booking returns 403")
    void guestCannotReadAnotherGuestsBooking() throws Exception {
        mockMvc.perform(get("/bookings/{id}", guestBBookingId).with(token("guest-a", "GUEST")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /bookings/{id} as STAFF returns another guest's booking")
    void staffCanReadAnotherGuestsBooking() throws Exception {
        mockMvc.perform(get("/bookings/{id}", guestBBookingId).with(token("staff", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guestBBookingId));
    }

    @Test
    @DisplayName("GET /bookings as STAFF returns all bookings")
    void staffListsAllBookings() throws Exception {
        mockMvc.perform(get("/bookings").with(token("staff", "STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                        guestABookingId.intValue(), guestBBookingId.intValue())));
    }

    @Test
    @DisplayName("GET /bookings/{id} as GUEST for an unknown booking returns 404")
    void unknownBookingReturns404ForGuest() throws Exception {
        mockMvc.perform(get("/bookings/{id}", Long.MAX_VALUE).with(token("guest-a", "GUEST")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("A matching ETag does not bypass the booking ownership check")
    void matchingEtagDoesNotBypassOwnershipCheck() throws Exception {
        mockMvc.perform(get("/bookings/{id}", guestBBookingId)
                        .with(token("guest-a", "GUEST"))
                        .header(HttpHeaders.IF_NONE_MATCH, "\"booking-" + guestBBookingId + "-REQUESTED\""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /bookings assigns ownership to the token subject")
    void createdBookingBelongsToTokenSubject() throws Exception {
        String location = mockMvc.perform(post("/bookings")
                        .with(token("guest-a", "GUEST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "restaurantId": %d,
                                  "guestName": "Guest A",
                                  "guestEmail": "guest-a@example.test",
                                  "dateTime": "2030-01-01T18:00:00",
                                  "partySize": 2
                                }
                                """.formatted(restaurantId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(location).isNotBlank();
        mockMvc.perform(get(location).with(token("guest-a", "GUEST")))
                .andExpect(status().isOk());
        mockMvc.perform(get(location).with(token("guest-b", "GUEST")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    private JwtRequestPostProcessor token(String subject, String role) {
        return jwt()
                .authorities(new RestaurantAuthoritiesConverter(TestTokenIssuer.ROLES_CLAIM))
                .jwt(j -> j.subject(subject)
                        .claim("scope", "bookings:write")
                        .claim(TestTokenIssuer.ROLES_CLAIM, List.of(role)));
    }
}
