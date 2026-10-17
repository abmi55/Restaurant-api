package se.edugrade.java25.restaurant.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public Restaurant(String name, String city, String cuisine, int seatCount) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.seatCount = seatCount;
    }

    public void update(String name, String city, String cuisine, int seatCount) {
        this.name = name;
        this.city = city;
        this.cuisine = cuisine;
        this.seatCount = seatCount;
    }

}
