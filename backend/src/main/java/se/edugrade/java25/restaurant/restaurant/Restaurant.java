package se.edugrade.java25.restaurant.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String cuisine;

    @Column(nullable = false)
    private int seatCount;

    @Column(nullable = false)
    private LocalTime weekdayOpensAt;

    @Column(nullable = false)
    private LocalTime weekdayClosesAt;

    @Column(nullable = false)
    private LocalTime weekendOpensAt;

    @Column(nullable = false)
    private LocalTime weekendClosesAt;

    public Restaurant(String name, String city, String cuisine, int seatCount,
                      LocalTime weekdayOpensAt, LocalTime weekdayClosesAt,
                      LocalTime weekendOpensAt, LocalTime weekendClosesAt) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.seatCount = seatCount;
        this.weekdayOpensAt = weekdayOpensAt;
        this.weekdayClosesAt = weekdayClosesAt;
        this.weekendOpensAt = weekendOpensAt;
        this.weekendClosesAt = weekendClosesAt;
    }

    public void update(String name, String city, String cuisine, int seatCount,
                       LocalTime weekdayOpensAt, LocalTime weekdayClosesAt,
                       LocalTime weekendOpensAt, LocalTime weekendClosesAt) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.seatCount = seatCount;
        this.weekdayOpensAt = weekdayOpensAt;
        this.weekdayClosesAt = weekdayClosesAt;
        this.weekendOpensAt = weekendOpensAt;
        this.weekendClosesAt = weekendClosesAt;
    }

    public LocalTime opensAt(DayOfWeek day) {
        return isWeekend(day) ? weekendOpensAt : weekdayOpensAt;
    }

    public LocalTime closesAt(DayOfWeek day) {
        return isWeekend(day) ? weekendClosesAt : weekdayClosesAt;
    }

    public static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

}
