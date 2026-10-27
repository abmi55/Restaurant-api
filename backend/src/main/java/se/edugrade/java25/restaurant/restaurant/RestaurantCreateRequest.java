package se.edugrade.java25.restaurant.restaurant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import se.edugrade.java25.restaurant.booking.Booking;

import java.time.Duration;
import java.time.LocalTime;

public record RestaurantCreateRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String cuisine,
        @Positive int seatCount,
        @NotNull @JsonFormat(pattern = "HH:mm", lenient = OptBoolean.FALSE) LocalTime weekdayOpensAt,
        @NotNull @JsonFormat(pattern = "HH:mm", lenient = OptBoolean.FALSE) LocalTime weekdayClosesAt,
        @NotNull @JsonFormat(pattern = "HH:mm", lenient = OptBoolean.FALSE) LocalTime weekendOpensAt,
        @NotNull @JsonFormat(pattern = "HH:mm", lenient = OptBoolean.FALSE) LocalTime weekendClosesAt
) {
    @JsonIgnore
    @AssertTrue(message = "Weekday opening hours must allow a full booking before closing")
    public boolean isWeekdayIntervalValid() {
        return weekdayOpensAt == null || weekdayClosesAt == null
                || Duration.between(weekdayOpensAt, weekdayClosesAt).compareTo(Booking.DURATION) >= 0;
    }

    @JsonIgnore
    @AssertTrue(message = "Weekend opening hours must allow a full booking before closing")
    public boolean isWeekendIntervalValid() {
        return weekendOpensAt == null || weekendClosesAt == null
                || Duration.between(weekendOpensAt, weekendClosesAt).compareTo(Booking.DURATION) >= 0;
    }
}
