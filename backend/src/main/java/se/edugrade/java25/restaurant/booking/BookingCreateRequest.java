package se.edugrade.java25.restaurant.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record BookingCreateRequest(
        @NotNull @Positive Long restaurantId,
        @NotBlank String guestName,
        @NotBlank @Email String guestEmail,
        @NotNull LocalDateTime dateTime,
        @Positive int partySize
) {
}
