package se.edugrade.java25.restaurant.booking;

public class InsufficientCapacityException extends RuntimeException {

    public InsufficientCapacityException(String message) {
        super(message);
    }
}
