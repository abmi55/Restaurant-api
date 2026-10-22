package se.edugrade.java25.restaurant.booking;

import org.springframework.stereotype.Component;
import se.edugrade.java25.restaurant.restaurant.Restaurant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CapacityChecker {

    private final BookingRepository bookingRepository;

    public CapacityChecker(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void requireSeatsFor(Restaurant restaurant, LocalDateTime start, int partySize) {
        List<Booking> overlapping = bookingRepository.findStartingBetween(
                restaurant.getId(), BookingStatus.seatOccupying(),
                start.minus(Booking.DURATION), start.plus(Booking.DURATION));
        if (peakOccupancy(overlapping, start) + partySize > restaurant.getSeatCount()) {
            throw new InsufficientCapacityException(
                    "There are not enough free seats for " + partySize + " guests at the selected time");
        }
    }

    public void requireRoomForSeatCount(Restaurant restaurant, int newSeatCount, LocalDateTime now) {
        List<Booking> current = bookingRepository.findStartingAfter(
                restaurant.getId(), BookingStatus.seatOccupying(), now.minus(Booking.DURATION));
        if (peakOccupancy(current, now) > newSeatCount) {
            throw new InsufficientCapacityException(
                    "The seat count cannot be lowered to " + newSeatCount
                            + " because existing bookings need more seats");
        }
    }

    // Occupancy changes only when a booking starts or ends.
    static int peakOccupancy(List<Booking> bookings, LocalDateTime from) {
        return Stream.concat(Stream.of(from),
                        bookings.stream().map(Booking::getDateTime).filter(start -> start.isAfter(from)))
                .mapToInt(moment -> occupancyAt(bookings, moment))
                .max()
                .orElse(0);
    }

    private static int occupancyAt(List<Booking> bookings, LocalDateTime moment) {
        return bookings.stream()
                .filter(booking -> !booking.getDateTime().isAfter(moment) && booking.endTime().isAfter(moment))
                .mapToInt(Booking::getPartySize)
                .sum();
    }
}
