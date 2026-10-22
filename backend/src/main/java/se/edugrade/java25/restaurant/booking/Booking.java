package se.edugrade.java25.restaurant.booking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.edugrade.java25.restaurant.restaurant.Restaurant;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_booking_restaurant_date_time", columnList = "restaurant_id, date_time"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    // Every booking occupies seats for the same length of time, for all restaurants.
    public static final Duration DURATION = Duration.ofMinutes(120);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestEmail;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private int partySize;

    // Auth0 user id (the token's sub claim) of the guest who made the booking.
    // Null for seed data and existing bookings, which only staff can see.
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    public Booking(
            Restaurant restaurant,
            String guestName,
            String guestEmail,
            LocalDateTime dateTime,
            int partySize,
            String ownerId
    ) {
        this.restaurant = restaurant;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.dateTime = dateTime;
        this.partySize = partySize;
        this.ownerId = ownerId;
        this.status = BookingStatus.REQUESTED;
    }

    public LocalDateTime endTime() {
        return dateTime.plus(DURATION);
    }

    public boolean transitionTo(BookingStatus target) {
        if (!status.canTransitionTo(target)) {
            return false;
        }

        status = target;
        return true;
    }

}
