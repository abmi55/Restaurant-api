package se.edugrade.java25.restaurant.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs {@link HttpNotificationService} over real HTTP against a WireMock stub
 * generated from notification-service's contract.
 *
 * <p>Stub Runner generates the stub at test time from the same contract file
 * that notification-service's {@code ContractVerifierTest} verifies. Reading the
 * contract directly, instead of a stub jar installed in {@code ~/.m2}, keeps
 * {@code mvn test} green on a fresh clone without a prior {@code mvn install}.
 *
 * <p>No Spring context is started: the test only needs the adapter and the
 * stub, not the database or the security configuration.
 */
class NotificationContractTest {

    private static final String GUEST_EMAIL = "guest@example.test";
    // Relative to backend/, where Surefire runs whether Maven starts from the root or from backend/.
    private static final String CONTRACTS_DIRECTORY = "../notification-service/src/test/resources/contracts";

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .downloadStub("se.edugrade.java25", "notification-service", "+", "stubs")
            .repoRoot("stubs://" + Path.of(CONTRACTS_DIRECTORY).toAbsolutePath().normalize().toUri())
            .stubsMode(StubRunnerProperties.StubsMode.REMOTE)
            .withGenerateStubs(true);

    private HttpNotificationService notificationService() {
        return new HttpNotificationService(stubRunner.findStubUrl("notification-service").toString());
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
