package se.edugrade.java25.restaurant.booking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.edugrade.java25.restaurant.restaurant.Restaurant;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    public Booking(
            Restaurant restaurant,
            String guestName,
            String guestEmail,
            LocalDateTime dateTime,
            int partySize
    ) {
        this.restaurant = restaurant;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.dateTime = dateTime;
        this.partySize = partySize;
        this.status = BookingStatus.REQUESTED;
    }

    public boolean transitionTo(BookingStatus target) {
        if (!status.canTransitionTo(target)) {
            return false;
        }

        status = target;
        return true;
    }

}
