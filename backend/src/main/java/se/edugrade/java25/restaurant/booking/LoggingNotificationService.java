package se.edugrade.java25.restaurant.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void bookingConfirmed(Long bookingId, String guestEmail) {
        logger.info("Booking {} confirmed, notification simulated", bookingId);
    }
}
