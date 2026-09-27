package se.edugrade.java25.restaurant.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;

import java.util.List;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
                          RestaurantRepository restaurantRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.restaurantId()));
        Booking booking = new Booking(
                restaurant,
                request.guestName(),
                request.guestEmail(),
                request.dateTime(),
                request.partySize()
        );

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking {} created for restaurant {} with status {}",
                savedBooking.getId(), restaurant.getId(), savedBooking.getStatus());
        return BookingResponse.from(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return BookingResponse.from(findBooking(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll().stream()
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

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
