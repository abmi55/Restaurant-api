package se.edugrade.java25.restaurant.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.restaurant.Restaurant;
import se.edugrade.java25.restaurant.restaurant.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.restaurant.RestaurantRepository;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;

    public BookingService(BookingRepository bookingRepository, RestaurantRepository restaurantRepository) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
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

        return BookingResponse.from(bookingRepository.save(booking));
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
        return transition(id, BookingStatus.CONFIRMED);
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
        if (!booking.transitionTo(targetStatus)) {
            throw new InvalidBookingStateException(id, targetStatus);
        }

        return BookingResponse.from(booking);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
