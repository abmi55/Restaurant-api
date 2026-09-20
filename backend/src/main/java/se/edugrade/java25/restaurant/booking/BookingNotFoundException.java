package se.edugrade.java25.restaurant.booking;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " was not found");
    }
}
