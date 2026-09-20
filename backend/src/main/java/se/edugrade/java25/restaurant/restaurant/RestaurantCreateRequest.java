package se.edugrade.java25.restaurant.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RestaurantCreateRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String cuisine,
        @Positive int seatCount
) {
}
