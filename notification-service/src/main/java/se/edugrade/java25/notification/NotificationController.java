package se.edugrade.java25.notification;

import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The whole public API of the mocked notification service.
 *
 * <p>The contract in {@code src/test/resources/contracts/notification/}
 * describes every response this controller produces. The generated
 * {@code ContractVerifierTest} fails this module's build if the response
 * drifts from the contract.
 */
@RestController
class NotificationController {

    @PostMapping("/notifications")
    NotificationReceipt accept(@RequestBody NotificationRequest request) {
        return new NotificationReceipt("msg-1001", "ACCEPTED", Instant.parse("2026-10-01T18:00:00Z"));
    }
}
