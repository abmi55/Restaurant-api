package se.edugrade.java25.restaurant.booking;

import java.util.Arrays;
import java.util.List;

public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    SEATED,
    CANCELLED;

    // SEATED guests still occupy seats until the booking interval ends.
    public boolean occupiesSeats() {
        return this != CANCELLED;
    }

    public static List<BookingStatus> seatOccupying() {
        return Arrays.stream(values()).filter(BookingStatus::occupiesSeats).toList();
    }

    public boolean canTransitionTo(BookingStatus target) {
        return switch (this) {
            case REQUESTED -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == SEATED || target == CANCELLED;
            case SEATED, CANCELLED -> false;
        };
    }
}
