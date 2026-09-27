package se.edugrade.java25.restaurant.booking;

public interface NotificationService {
    void bookingConfirmed(Long bookingId, String guestEmail);
}
