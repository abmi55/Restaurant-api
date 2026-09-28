package se.edugrade.java25.restaurant.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs {@link HttpNotificationService} over real HTTP against a WireMock stub
 * generated from notification-service's contract.
 *
 * <p>{@code @AutoConfigureStubRunner} resolves notification-service's stub jar
 * by Maven coordinates and starts it on a free port ({@code 0}).
 * {@link StubFinder} looks that port up. {@code StubsMode.LOCAL} reads the jar
 * from {@code ~/.m2}, where it only arrives in the install phase, so run
 * {@code mvn clean install} from the repository root before this test.
 */
@SpringBootTest
@AutoConfigureStubRunner(
        ids = "se.edugrade.java25:notification-service:+:stubs:0",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
class NotificationContractTest {

    private static final String GUEST_EMAIL = "guest@example.test";

    @Autowired
    private StubFinder stubFinder;

    private HttpNotificationService notificationService() {
        return new HttpNotificationService(stubFinder.findStubUrl("notification-service").toString());
    }

    @Test
    @DisplayName("the adapter accepts the provider's contract-verified ACCEPTED receipt")
    void bookingConfirmedIsAcceptedByTheProvider() {
        // Same body as the contract, so the stub answers with its receipt.
        assertDoesNotThrow(() -> notificationService().bookingConfirmed(1L, GUEST_EMAIL));
    }

    @Test
    @DisplayName("the stub rejects a request that does not match the contract")
    void requestOutsideTheContractIsRejected() {
        // WireMock has no mapping for this body and answers 404.
        assertThrows(HttpClientErrorException.NotFound.class,
                () -> notificationService().bookingConfirmed(999L, GUEST_EMAIL));
    }
}
