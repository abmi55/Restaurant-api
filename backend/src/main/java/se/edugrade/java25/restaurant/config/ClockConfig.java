package se.edugrade.java25.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    // Booking times are Swedish local times, even when the JVM runs in UTC.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Stockholm"));
    }
}
