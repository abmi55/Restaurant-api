package se.edugrade.java25.restaurant.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;
    private final CapacityChecker capacityChecker;
    private final Clock clock;
    private final int maxPartySize;

    public BookingService(BookingRepository bookingRepository,
                          RestaurantRepository restaurantRepository,
                          NotificationService notificationService,
                          CapacityChecker capacityChecker, Clock clock,
                          @Value("${app.booking.max-party-size}") int maxPartySize) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.notificationService = notificationService;
        this.capacityChecker = capacityChecker;
        this.clock = clock;
        this.maxPartySize = maxPartySize;
    }

    @Transactional
    public BookingResponse create(BookingCreateRequest request, String ownerId) {
        requireFutureTime(request.dateTime());
        requirePartySizeWithinLimit(request.partySize());
        Restaurant restaurant = restaurantRepository.findByIdForUpdate(request.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.restaurantId()));
        if (request.partySize() > restaurant.getSeatCount()) {
            throw new InvalidBookingRequestException(
                    "Party size exceeds the restaurant's " + restaurant.getSeatCount() + " seats");
        }
        requireWithinOpeningHours(restaurant, request.dateTime());
        capacityChecker.requireSeatsFor(restaurant, request.dateTime(), request.partySize());
        Booking booking = new Booking(
                restaurant,
                request.guestName(),
                request.guestEmail(),
                request.dateTime(),
                request.partySize(),
                ownerId
        );

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking {} created for restaurant {} with status {}",
                savedBooking.getId(), restaurant.getId(), savedBooking.getStatus());
        return BookingResponse.from(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id, String requesterId, boolean staff) {
        Booking booking = findBooking(id);
        if (!staff && !requesterId.equals(booking.getOwnerId())) {
            throw new BookingAccessDeniedException(id);
        }
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAll(String requesterId, boolean staff) {
        List<Booking> bookings = staff
                ? bookingRepository.findAll()
                : bookingRepository.findByOwnerId(requesterId);
        return bookings.stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse confirm(Long id) {
        BookingResponse response = transition(id, BookingStatus.CONFIRMED);
        notificationService.bookingConfirmed(response.id(), response.guestEmail());
        return response;
    }

    @Transactional
    public BookingResponse seat(Long id) {
        return transition(id, BookingStatus.SEATED);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        return transition(id, BookingStatus.CANCELLED);
    }

    private BookingResponse transition(Long id, BookingStatus targetStatus) {
        Booking booking = findBooking(id);
        BookingStatus previousStatus = booking.getStatus();
        if (!booking.transitionTo(targetStatus)) {
            throw new InvalidBookingStateException(id, targetStatus);
        }

        logger.info("Booking {} status changed from {} to {}", id, previousStatus, booking.getStatus());
        return BookingResponse.from(booking);
    }

    private void requireWithinOpeningHours(Restaurant restaurant, LocalDateTime dateTime) {
        var day = dateTime.getDayOfWeek();
        var opensAt = dateTime.toLocalDate().atTime(restaurant.opensAt(day));
        var closesAt = dateTime.toLocalDate().atTime(restaurant.closesAt(day));
        if (dateTime.isBefore(opensAt) || dateTime.plus(Booking.DURATION).isAfter(closesAt)) {
            throw new InvalidBookingRequestException(
                    "On " + (Restaurant.isWeekend(day) ? "weekends" : "weekdays")
                            + " the restaurant is open " + opensAt.toLocalTime() + "–" + closesAt.toLocalTime()
                            + ". Bookings must start between " + opensAt.toLocalTime()
                            + " and " + closesAt.minus(Booking.DURATION).toLocalTime() + ".");
        }
    }

    private void requireFutureTime(LocalDateTime dateTime) {
        if (!dateTime.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidBookingRequestException("Booking time must be in the future");
        }
    }

    private void requirePartySizeWithinLimit(int partySize) {
        if (partySize > maxPartySize) {
            throw new InvalidBookingRequestException(
                    "Online bookings are limited to " + maxPartySize
                            + " guests. Contact the restaurant for larger parties.");
        }
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
