package se.edugrade.java25.restaurant.booking;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingApiTest {

    private static final String GUEST_NAME = "Test Guest";
    private static final String GUEST_EMAIL = "guest@example.test";
    private static final String DATE_TIME = "2030-01-01T18:00:00";
    private static final int PARTY_SIZE = 2;

    @LocalServerPort
    private int port;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Long restaurantId;

    // Real HTTP means the server commits its own transactions, so test data is cleaned explicitly.
    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        restaurantRepository.deleteAll();
        restaurantId = restaurantRepository.save(new Restaurant("Test Restaurant", "Stockholm", "Swedish", 20)).getId();
    }

    @Test
    @DisplayName("POST /bookings returns 201 with Location and REQUESTED booking")
    void createBookingReturns201AndLocation() {
        Response response = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(bookingJson())
        .when()
                .post("/bookings")
        .then()
                .statusCode(201)
                .extract()
                .response();

        Long bookingId = response.jsonPath().getObject("id", Long.class);
        assertNotNull(bookingId);
        assertEquals("http://localhost:" + port + "/bookings/" + bookingId, response.header("Location"));
        assertRequestedBooking(response.jsonPath(), bookingId);
    }

    @Test
    @DisplayName("GET /bookings/{id} returns 200 with the booking")
    void getBookingReturns200() {
        long bookingId = createBooking();

        JsonPath booking = given()
                .port(port)
        .when()
                .get("/bookings/{id}", bookingId)
        .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertRequestedBooking(booking, bookingId);
    }

    @Test
    @DisplayName("GET /bookings/{id} for unknown booking returns 404 ProblemDetail")
    void getUnknownBookingReturns404ProblemDetail() {
        long unknownId = Long.MAX_VALUE;
        assertFalse(bookingRepository.existsById(unknownId));

        given()
                .port(port)
        .when()
                .get("/bookings/{id}", unknownId)
        .then()
                .statusCode(404)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(404))
                .body("detail", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("Confirming a booking persists CONFIRMED and notifies the guest once")
    void confirmBookingNotifiesGuest() {
        long bookingId = createBooking();

        given()
                .port(port)
        .when()
                .post("/bookings/{id}/confirm", bookingId)
        .then()
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"));

        given()
                .port(port)
        .when()
                .get("/bookings/{id}", bookingId)
        .then()
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"));

        verify(notificationService).bookingConfirmed(bookingId, GUEST_EMAIL);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Confirming a cancelled booking returns 409 without notification")
    void confirmCancelledBookingReturns409WithoutNotification() {
        long bookingId = createBooking();

        given()
                .port(port)
        .when()
                .post("/bookings/{id}/cancel", bookingId)
        .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));

        given()
                .port(port)
        .when()
                .post("/bookings/{id}/confirm", bookingId)
        .then()
                .statusCode(409)
                .contentType(startsWith("application/problem+json"))
                .body("status", equalTo(409));

        given()
                .port(port)
        .when()
                .get("/bookings/{id}", bookingId)
        .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));

        verifyNoInteractions(notificationService);
    }

    private String bookingJson() {
        return """
                {
                  "restaurantId": %d,
                  "guestName": "%s",
                  "guestEmail": "%s",
                  "dateTime": "%s",
                  "partySize": %d
                }
                """.formatted(restaurantId, GUEST_NAME, GUEST_EMAIL, DATE_TIME, PARTY_SIZE);
    }

    private long createBooking() {
        return given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(bookingJson())
        .when()
                .post("/bookings")
        .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private void assertRequestedBooking(JsonPath booking, long expectedId) {
        assertEquals(expectedId, booking.getLong("id"));
        assertEquals(restaurantId, booking.getLong("restaurantId"));
        assertEquals(GUEST_NAME, booking.getString("guestName"));
        assertEquals(GUEST_EMAIL, booking.getString("guestEmail"));
        // Compared as LocalDateTime so the JSON format may omit or include zero seconds.
        assertEquals(LocalDateTime.parse(DATE_TIME), LocalDateTime.parse(booking.getString("dateTime")));
        assertEquals(PARTY_SIZE, booking.getInt("partySize"));
        assertEquals("REQUESTED", booking.getString("status"));
    }
}
