package se.edugrade.java25.restaurant.restaurant;

public record RestaurantResponse(
        Long id,
        String name,
        String city,
        String cuisine,
        int seatCount
) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCity(),
                restaurant.getCuisine(),
                restaurant.getSeatCount()
        );
    }
}
