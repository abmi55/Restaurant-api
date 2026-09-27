package se.edugrade.java25.restaurant.booking;

import org.springframework.web.client.RestClient;

/**
 * A {@link NotificationService} that calls the notification service over HTTP
 * and interprets its receipt.
 *
 * <p>Deliberately not a Spring bean: no real notification service exists, so
 * the running application keeps {@link LoggingNotificationService}. This class
 * is used only by {@code NotificationContractTest}, which points it at a
 * WireMock stub generated from the notification service's verified contract.
 * A green test therefore proves this HTTP exchange matches what the provider
 * actually promises.
 */
public class HttpNotificationService implements NotificationService {

    private static final String ACCEPTED = "ACCEPTED";

    private record NotificationRequestBody(Long bookingId, String guestEmail) {
    }

    private record NotificationReceiptBody(String messageId, String status) {
    }

    private final RestClient restClient;

    public HttpNotificationService(String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void bookingConfirmed(Long bookingId, String guestEmail) {
        NotificationReceiptBody receipt = restClient.post()
                .uri("/notifications")
                .body(new NotificationRequestBody(bookingId, guestEmail))
                .retrieve()
                .body(NotificationReceiptBody.class);

        String status = receipt == null ? null : receipt.status();
        if (!ACCEPTED.equals(status)) {
            throw new IllegalStateException(
                    "Notification for booking " + bookingId + " not accepted: " + status);
        }
    }
}
