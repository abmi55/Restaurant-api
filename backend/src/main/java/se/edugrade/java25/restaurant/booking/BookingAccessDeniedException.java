package se.edugrade.java25.restaurant.booking;

public class BookingAccessDeniedException extends RuntimeException {
    public BookingAccessDeniedException(Long id) {
        super("You do not have access to booking " + id);
    }
}
