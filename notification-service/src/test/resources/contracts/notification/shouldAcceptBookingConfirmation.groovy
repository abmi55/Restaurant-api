import org.springframework.cloud.contract.spec.Contract

// Contract: POST /notifications accepts a booking confirmation and answers
// with a receipt. The restaurant API depends on "status": it treats the
// notification as delivered only when the value is exactly "ACCEPTED".
//
// A stub generated from this contract answers only a request that matches
// the body below.
Contract.make {
    description "should accept a booking confirmation and answer ACCEPTED"
    request {
        method POST()
        url "/notifications"
        headers { contentType applicationJson() }
        body(
            bookingId: 1,
            guestEmail: "guest@example.test"
        )
    }
    response {
        status OK()
        headers { contentType applicationJson() }
        body(
            messageId: "msg-1001",
            status: "ACCEPTED",
            acceptedAt: "2026-10-01T18:00:00Z"
        )
    }
}
