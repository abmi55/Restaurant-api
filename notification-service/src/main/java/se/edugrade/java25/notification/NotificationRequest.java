package se.edugrade.java25.notification;

/**
 * What the restaurant API asks this service to deliver when a booking is
 * confirmed.
 *
 * <p>Same fields as {@code NotificationService.bookingConfirmed} in the API.
 * The two sides share no code, only this shape, which the contract pins down.
 */
public record NotificationRequest(Long bookingId, String guestEmail) {
}
