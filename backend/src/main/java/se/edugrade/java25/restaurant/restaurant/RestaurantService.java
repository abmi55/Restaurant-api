package se.edugrade.java25.restaurant.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.booking.CapacityChecker;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final CapacityChecker capacityChecker;
    private final Clock clock;

    public RestaurantService(RestaurantRepository restaurantRepository, CapacityChecker capacityChecker, Clock clock) {
        this.restaurantRepository = restaurantRepository;
        this.capacityChecker = capacityChecker;
        this.clock = clock;
    }

    @Transactional
    public RestaurantResponse create(RestaurantCreateRequest request) {
        Restaurant restaurant = new Restaurant(
                request.name(),
                request.city(),
                request.cuisine(),
                request.seatCount()
        );

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant {} created", savedRestaurant.getId());
        return RestaurantResponse.from(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse update(Long id, RestaurantCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        // Existing overbooked restaurants can still change details or raise capacity.
        if (request.seatCount() < restaurant.getSeatCount()) {
            capacityChecker.requireRoomForSeatCount(restaurant, request.seatCount(), LocalDateTime.now(clock));
        }
        restaurant.update(request.name(), request.city(), request.cuisine(), request.seatCount());
        logger.info("Restaurant {} updated", restaurant.getId());
        return RestaurantResponse.from(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {
        return RestaurantResponse.from(findRestaurant(id));
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAll(Pageable pageable) {
        return restaurantRepository.findAll(pageable).map(RestaurantResponse::from);
    }

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
