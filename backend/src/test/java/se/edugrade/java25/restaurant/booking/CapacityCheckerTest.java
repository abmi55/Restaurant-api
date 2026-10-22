package se.edugrade.java25.restaurant.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapacityCheckerTest {

    @Test
    @DisplayName("No bookings means zero occupancy")
    void emptyBookingsHaveNoOccupancy() {
        assertThat(CapacityChecker.peakOccupancy(List.of(), at(1, 18, 0))).isZero();
    }

    @Test
    @DisplayName("Bookings that do not overlap each other are not added together")
    void separateOverlapsAreNotAddedTogether() {
        assertThat(CapacityChecker.peakOccupancy(List.of(
                booking(1, 17, 0, 25), booking(1, 19, 0, 25)), at(1, 18, 0)))
                .isEqualTo(25);
    }

    @Test
    @DisplayName("A booking ending at the requested start time does not occupy seats")
    void bookingEndingAtStartDoesNotOccupySeats() {
        assertThat(CapacityChecker.peakOccupancy(List.of(
                booking(1, 16, 0, 8)), at(1, 18, 0)))
                .isZero();
    }

    @Test
    @DisplayName("A booking starting later in the requested interval can set peak occupancy")
    void laterBookingStartCanSetPeak() {
        assertThat(CapacityChecker.peakOccupancy(List.of(
                booking(1, 19, 0, 8)), at(1, 18, 0)))
                .isEqualTo(8);
    }

    @Test
    @DisplayName("A booking spanning midnight counts toward occupancy the next day")
    void bookingCanSpanMidnight() {
        assertThat(CapacityChecker.peakOccupancy(List.of(
                booking(1, 23, 30, 8)), at(2, 0, 30)))
                .isEqualTo(8);
    }

    @Test
    @DisplayName("Overlapping bookings are added together at the later start time")
    void simultaneousBookingsAreAddedAtLaterStart() {
        assertThat(CapacityChecker.peakOccupancy(List.of(
                booking(1, 18, 0, 4), booking(1, 19, 0, 5)), at(1, 18, 0)))
                .isEqualTo(9);
    }

    private static Booking booking(int day, int hour, int minute, int partySize) {
        return new Booking(null, "Guest", "guest@example.test", at(day, hour, minute), partySize, null);
    }

    private static LocalDateTime at(int day, int hour, int minute) {
        return LocalDateTime.of(2030, 1, day, hour, minute);
    }
}
