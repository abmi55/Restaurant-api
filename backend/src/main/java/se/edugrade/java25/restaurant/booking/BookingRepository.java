package se.edugrade.java25.restaurant.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByOwnerId(String ownerId);

    @Query("""
            select b from Booking b
            where b.restaurant.id = :restaurantId
              and b.status in :statuses
              and b.dateTime > :startsAfter
              and b.dateTime < :startsBefore
            """)
    List<Booking> findStartingBetween(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("startsAfter") LocalDateTime startsAfter,
            @Param("startsBefore") LocalDateTime startsBefore);

    @Query("""
            select b from Booking b
            where b.restaurant.id = :restaurantId
              and b.status in :statuses
              and b.dateTime > :startsAfter
            """)
    List<Booking> findStartingAfter(
            @Param("restaurantId") Long restaurantId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("startsAfter") LocalDateTime startsAfter);
}
