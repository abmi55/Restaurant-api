package se.edugrade.java25.restaurant.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import se.edugrade.java25.restaurant.booking.Booking;
import se.edugrade.java25.restaurant.booking.BookingRepository;
import se.edugrade.java25.restaurant.booking.NotificationService;
import se.edugrade.java25.restaurant.config.RestaurantAuthoritiesConverter;
import se.edugrade.java25.restaurant.security.TestTokenIssuer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantOpeningHoursTest {

    @Autowired MockMvc mvc;
    @Autowired BookingRepository bookings;
    @Autowired RestaurantRepository restaurants;
    @MockitoBean NotificationService notifications;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        bookings.deleteAll();
        restaurants.deleteAll();
        restaurant = restaurants.save(new Restaurant("Test", "Stockholm", "Swedish", 10,
                LocalTime.of(11, 0), LocalTime.of(23, 0), LocalTime.of(12, 0), LocalTime.of(22, 0)));
    }

    @ParameterizedTest
    @DisplayName("All opening hours are required and PUT validates the same request as POST")
    @CsvSource({
            "POST, weekdayOpensAt", "POST, weekdayClosesAt",
            "POST, weekendOpensAt", "POST, weekendClosesAt",
            "PUT, weekdayOpensAt"
    })
    void everyOpeningHoursFieldIsRequired(String method, String field) throws Exception {
        String body = restaurantJson("11:00", "23:00", "12:00", "22:00")
                .replaceFirst(",\\s*\"" + field + "\": \"[^\"]+\"", "");

        write(method, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
        assertThat(restaurants.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @DisplayName("Weekday and weekend intervals shorter than two hours or crossing midnight return 400")
    @CsvSource({
            "11:00, 12:59, 12:00, 22:00",
            "11:00, 23:00, 12:00, 13:59",
            "23:00, 01:00, 12:00, 22:00",
            "11:00, 23:00, 22:00, 02:00"
    })
    void tooShortOrOvernightIntervalsAreRejected(String weekdayOpen, String weekdayClose,
                                                String weekendOpen, String weekendClose) throws Exception {
        String body = restaurantJson(weekdayOpen, weekdayClose, weekendOpen, weekendClose);
        write("POST", body).andExpect(status().isBadRequest());
        assertThat(restaurants.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opening intervals of exactly two hours are accepted for weekdays and weekends")
    void exactlyTwoHoursAreAcceptedForBothDayTypes() throws Exception {
        write("POST", restaurantJson("11:00", "13:00", "12:00", "14:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weekdayOpensAt").value("11:00"))
                .andExpect(jsonPath("$.weekdayClosesAt").value("13:00"))
                .andExpect(jsonPath("$.weekendOpensAt").value("12:00"))
                .andExpect(jsonPath("$.weekendClosesAt").value("14:00"));
    }

    @ParameterizedTest
    @DisplayName("Invalid hours and times outside HH:mm return 400 ProblemDetail")
    @ValueSource(strings = {"25:00", "24:00", "11:00:30"})
    void invalidTimeReturnsBadRequestProblemDetail(String time) throws Exception {
        write("POST", restaurantJson(time, "23:00", "12:00", "22:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Restaurant detail and list responses include all opening hours as HH:mm")
    void detailAndListReturnAllOpeningHoursInHourMinuteFormat() throws Exception {
        mvc.perform(get("/restaurants/{id}", restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekdayOpensAt").value("11:00"))
                .andExpect(jsonPath("$.weekdayClosesAt").value("23:00"))
                .andExpect(jsonPath("$.weekendOpensAt").value("12:00"))
                .andExpect(jsonPath("$.weekendClosesAt").value("22:00"));
        mvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].weekdayOpensAt").value("11:00"))
                .andExpect(jsonPath("$.content[0].weekdayClosesAt").value("23:00"))
                .andExpect(jsonPath("$.content[0].weekendOpensAt").value("12:00"))
                .andExpect(jsonPath("$.content[0].weekendClosesAt").value("22:00"));
    }

    @Test
    @DisplayName("Updating opening hours preserves existing bookings outside the new interval")
    void changingHoursPreservesExistingBookingOutsideNewHours() throws Exception {
        var dateTime = LocalDateTime.of(2030, 1, 2, 18, 0);
        Booking booking = bookings.save(new Booking(restaurant, "Guest", "guest@example.test", dateTime, 2, "guest"));

        write("PUT", restaurantJson("11:00", "13:00", "12:00", "14:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekdayClosesAt").value("13:00"))
                .andExpect(jsonPath("$.weekendClosesAt").value("14:00"));

        assertThat(bookings.findById(booking.getId()).orElseThrow().getDateTime()).isEqualTo(dateTime);
        assertThat(bookings.count()).isEqualTo(1);
        assertThat(restaurants.findById(restaurant.getId()).orElseThrow().getWeekdayClosesAt())
                .isEqualTo(LocalTime.of(13, 0));
    }

    private ResultActions write(String method, String body) throws Exception {
        String path = method.equals("POST") ? "/restaurants" : "/restaurants/" + restaurant.getId();
        return mvc.perform(request(HttpMethod.valueOf(method), path)
                .with(jwt().authorities(new RestaurantAuthoritiesConverter(TestTokenIssuer.ROLES_CLAIM))
                        .jwt(j -> j.subject("admin").claim("scope", "restaurants:write")
                                .claim(TestTokenIssuer.ROLES_CLAIM, List.of("ADMIN"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String restaurantJson(String weekdayOpen, String weekdayClose, String weekendOpen, String weekendClose) {
        return """
                {"name": "Test", "city": "Stockholm", "cuisine": "Swedish", "seatCount": 10,
                 "weekdayOpensAt": "%s", "weekdayClosesAt": "%s",
                 "weekendOpensAt": "%s", "weekendClosesAt": "%s"}
                """.formatted(weekdayOpen, weekdayClose, weekendOpen, weekendClose);
    }
}
