package se.edugrade.java25.restaurant.restaurant;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record RestaurantResponse(
        Long id,
        String name,
        String city,
        String cuisine,
        int seatCount,
        @JsonFormat(pattern = "HH:mm") LocalTime weekdayOpensAt,
        @JsonFormat(pattern = "HH:mm") LocalTime weekdayClosesAt,
        @JsonFormat(pattern = "HH:mm") LocalTime weekendOpensAt,
        @JsonFormat(pattern = "HH:mm") LocalTime weekendClosesAt
) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCity(),
                restaurant.getCuisine(),
                restaurant.getSeatCount(),
                restaurant.getWeekdayOpensAt(),
                restaurant.getWeekdayClosesAt(),
                restaurant.getWeekendOpensAt(),
                restaurant.getWeekendClosesAt()
        );
    }
}
