package se.edugrade.java25.restaurant.booking;

public class InvalidBookingStateException extends RuntimeException {

    public InvalidBookingStateException(Long id, BookingStatus targetStatus) {
        super("Booking with id " + id + " cannot transition to " + targetStatus);
    }
}
