package se.edugrade.java25.restaurant.booking;

public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    SEATED,
    CANCELLED;

    public boolean canTransitionTo(BookingStatus target) {
        return switch (this) {
            case REQUESTED -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == SEATED || target == CANCELLED;
            case SEATED, CANCELLED -> false;
        };
    }
}
