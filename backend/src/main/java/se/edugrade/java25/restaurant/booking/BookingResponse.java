package se.edugrade.java25.restaurant.booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long restaurantId,
        String guestName,
        String guestEmail,
        LocalDateTime dateTime,
        int partySize,
        BookingStatus status
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRestaurant().getId(),
                booking.getGuestName(),
                booking.getGuestEmail(),
                booking.getDateTime(),
                booking.getPartySize(),
                booking.getStatus()
        );
    }
}
