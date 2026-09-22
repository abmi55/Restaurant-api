package se.edugrade.java25.restaurant.booking;

import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingModelAssembler bookingModelAssembler;

    public BookingController(
            BookingService bookingService,
            BookingModelAssembler bookingModelAssembler
    ) {
        this.bookingService = bookingService;
        this.bookingModelAssembler = bookingModelAssembler;
    }

    @GetMapping
    public ResponseEntity<List<EntityModel<BookingResponse>>> getAll() {
        List<EntityModel<BookingResponse>> bookings = bookingService.getAll().stream()
                .map(bookingModelAssembler::toModel)
                .toList();

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<BookingResponse>> getById(
            @PathVariable Long id,
            WebRequest webRequest) {
        BookingResponse response = bookingService.getById(id);
        String etag = createEtag(response);

        if (webRequest.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .body(bookingModelAssembler.toModel(response));
    }

    @PostMapping
    public ResponseEntity<EntityModel<BookingResponse>> create(
            @Valid @RequestBody BookingCreateRequest request
    ) {
        BookingResponse response = bookingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(bookingModelAssembler.toModel(response));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<EntityModel<BookingResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingModelAssembler.toModel(bookingService.confirm(id)));
    }

    @PostMapping("/{id}/seat")
    public ResponseEntity<EntityModel<BookingResponse>> seat(@PathVariable Long id) {
        return ResponseEntity.ok(bookingModelAssembler.toModel(bookingService.seat(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EntityModel<BookingResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingModelAssembler.toModel(bookingService.cancel(id)));
    }

    private String createEtag(BookingResponse response) {
        return "\"booking-" + response.id() + "-" + response.status() + "\"";
    }
}
