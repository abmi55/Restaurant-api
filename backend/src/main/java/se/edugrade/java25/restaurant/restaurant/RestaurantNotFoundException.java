package se.edugrade.java25.restaurant.restaurant;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long id) {
        super("Restaurant with id " + id + " was not found");
    }
}
