package se.edugrade.java25.restaurant.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import se.edugrade.java25.restaurant.config.RestaurantAuthoritiesConverter;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;
import se.edugrade.java25.restaurant.security.TestTokenIssuer;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.properties.jakarta.persistence.lock.timeout=500")
@AutoConfigureMockMvc
@Timeout(10)
class BookingCapacityTest {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookings;
    @Autowired RestaurantRepository restaurants;
    @Autowired TransactionTemplate transactions;
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
    @DisplayName("A booking that exactly fills the restaurant capacity returns 201")
    void exactCapacityIsAccepted() throws Exception {
        existing(restaurant, "2030-01-01T18:00", 6, BookingStatus.REQUESTED);
        create(restaurant.getId(), "2030-01-01T18:00", 4).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Insufficient capacity returns 409 without exposing existing guest details")
    void insufficientCapacityIsConflictWithoutGuestDetails() throws Exception {
        existing(restaurant, "2030-01-01T18:00", 6, BookingStatus.REQUESTED);
        create(restaurant.getId(), "2030-01-01T18:00", 5)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", not(containsString("Existing Guest"))))
                .andExpect(jsonPath("$.detail", not(containsString("existing@example.test"))));
        assertThat(bookings.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bookings at another restaurant do not use the requested restaurant capacity")
    void otherRestaurantDoesNotUseCapacity() throws Exception {
        Restaurant other = restaurants.save(new Restaurant("Other", "Stockholm", "Swedish", 10));
        existing(other, "2030-01-01T18:00", 10, BookingStatus.REQUESTED);
        create(restaurant.getId(), "2030-01-01T18:00", 4).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Cancelled bookings do not use restaurant capacity")
    void cancelledBookingDoesNotUseCapacity() throws Exception {
        existing(restaurant, "2030-01-01T18:00", 10, BookingStatus.CANCELLED);
        create(restaurant.getId(), "2030-01-01T18:00", 4).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @DisplayName("CONFIRMED and SEATED bookings use restaurant capacity")
    @EnumSource(value = BookingStatus.class, names = {"CONFIRMED", "SEATED"})
    void activeStatusUsesCapacity(BookingStatus bookingStatus) throws Exception {
        existing(restaurant, "2030-01-01T18:00", 10, bookingStatus);
        create(restaurant.getId(), "2030-01-01T18:00", 1).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A booking that exceeds capacity during its time interval returns 409")
    void overlapInMiddleIsRejected() throws Exception {
        existing(restaurant, "2030-01-01T19:00", 8, BookingStatus.REQUESTED);
        create(restaurant.getId(), "2030-01-01T18:00", 4).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A booking in the past returns 400")
    void pastTimeIsRejected() throws Exception {
        create(restaurant.getId(), "2030-01-01T11:00", 2).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("A booking at the current time returns 400")
    void currentTimeIsRejected() throws Exception {
        create(restaurant.getId(), "2030-01-01T12:00", 2).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("The party size limit is checked before looking up the restaurant")
    void partyLimitIsCheckedBeforeRestaurantExists() throws Exception {
        create(Long.MAX_VALUE, "2030-01-01T18:00", 9)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Contact the restaurant")));
    }

    @Test
    @DisplayName("A party larger than the restaurant capacity returns 400")
    void partyLargerThanRestaurantIsBadRequest() throws Exception {
        Restaurant small = restaurants.save(new Restaurant("Small", "Stockholm", "Swedish", 6));
        create(small.getId(), "2030-01-01T18:00", 7).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("A booking for an unknown restaurant returns 404")
    void unknownRestaurantIsNotFound() throws Exception {
        create(Long.MAX_VALUE, "2030-01-01T18:00", 4).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Confirming and seating an existing booking do not recheck capacity")
    void confirmationAndSeatingDoNotRecheckCapacity() throws Exception {
        Booking booking = existing(restaurant, "2030-01-01T18:00", 10, BookingStatus.REQUESTED);
        mvc.perform(post("/bookings/{id}/confirm", booking.getId()).with(token("STAFF", "bookings:manage")))
                .andExpect(status().isOk());
        mvc.perform(post("/bookings/{id}/seat", booking.getId()).with(token("STAFF", "bookings:manage")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A concurrent booking checks capacity after the first transaction commits")
    void concurrentBookingSeesSeatsCommittedByFirstTransaction() throws Exception {
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> transactions.executeWithoutResult(status -> {
                restaurants.findByIdForUpdate(restaurant.getId()).orElseThrow();
                existing(restaurant, "2030-01-01T18:00", 8, BookingStatus.REQUESTED);
                locked.countDown();
                await(release);
            }));
            assertThat(locked.await(2, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> create(restaurant.getId(), "2030-01-01T18:00", 4)
                    .andExpect(status().isConflict()));
            Thread.sleep(300);
            assertThat(second.isDone()).isFalse();
            release.countDown();
            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
            assertThat(bookings.count()).isEqualTo(1);
        } finally {
            release.countDown();
        }
    }

    @Test
    @DisplayName("A concurrent seat reduction checks bookings after the first transaction commits")
    void concurrentSeatReductionSeesBookingCommittedByFirstTransaction() throws Exception {
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> transactions.executeWithoutResult(status -> {
                restaurants.findByIdForUpdate(restaurant.getId()).orElseThrow();
                existing(restaurant, "2030-01-01T18:00", 8, BookingStatus.REQUESTED);
                locked.countDown();
                await(release);
            }));
            assertThat(locked.await(2, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> mvc.perform(put("/restaurants/{id}", restaurant.getId())
                    .with(token("ADMIN", "restaurants:write"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"name": "Test", "city": "Stockholm", "cuisine": "Swedish", "seatCount": 6}
                            """))
                    .andExpect(status().isConflict()));
            Thread.sleep(300);
            assertThat(second.isDone()).isFalse();
            release.countDown();
            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
            assertThat(restaurants.findById(restaurant.getId()).orElseThrow().getSeatCount()).isEqualTo(10);
        } finally {
            release.countDown();
        }
    }

    @Test
    @DisplayName("A lock timeout returns 503 without saving the booking")
    void lockTimeoutReturns503WithoutSavingBooking() throws Exception {
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> transactions.executeWithoutResult(status -> {
                restaurants.findByIdForUpdate(restaurant.getId()).orElseThrow();
                locked.countDown();
                await(release);
            }));
            assertThat(locked.await(2, TimeUnit.SECONDS)).isTrue();
            create(restaurant.getId(), "2030-01-01T18:00", 4)
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
            release.countDown();
            first.get(2, TimeUnit.SECONDS);
            assertThat(bookings.count()).isZero();
        } finally {
            release.countDown();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(4, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test latch");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private ResultActions create(Long restaurantId, String dateTime, int partySize) throws Exception {
        return mvc.perform(post("/bookings")
                .with(token("GUEST", "bookings:write"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"restaurantId": %d, "guestName": "New Guest", "guestEmail": "new@example.test",
                         "dateTime": "%s", "partySize": %d}
                        """.formatted(restaurantId, dateTime, partySize)));
    }

    private Booking existing(Restaurant forRestaurant, String dateTime, int partySize, BookingStatus bookingStatus) {
        Booking booking = new Booking(forRestaurant, "Existing Guest", "existing@example.test",
                LocalDateTime.parse(dateTime), partySize, null);
        if (bookingStatus == BookingStatus.CONFIRMED || bookingStatus == BookingStatus.SEATED) {
            booking.transitionTo(BookingStatus.CONFIRMED);
        }
        if (bookingStatus == BookingStatus.SEATED) {
            booking.transitionTo(BookingStatus.SEATED);
        }
        if (bookingStatus == BookingStatus.CANCELLED) {
            booking.transitionTo(BookingStatus.CANCELLED);
        }
        return bookings.save(booking);
    }

    private JwtRequestPostProcessor token(String role, String scope) {
        return jwt().authorities(new RestaurantAuthoritiesConverter(TestTokenIssuer.ROLES_CLAIM))
                .jwt(j -> j.subject("test-user").claim("scope", scope)
                        .claim(TestTokenIssuer.ROLES_CLAIM, List.of(role)));
    }
}
