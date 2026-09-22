package se.edugrade.java25.restaurant.booking;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookingModelAssembler implements
        RepresentationModelAssembler<BookingResponse, EntityModel<BookingResponse>> {

    @Override
    public EntityModel<BookingResponse> toModel(BookingResponse response) {
        return EntityModel.of(response, linksFor(response));
    }

    private List<Link> linksFor(BookingResponse response) {
        List<Link> links = new ArrayList<>();

        if (response.status().canTransitionTo(BookingStatus.CONFIRMED)) {
            links.add(linkTo(methodOn(BookingController.class)
                    .confirm(response.id())).withRel("confirm"));
        }

        if (response.status().canTransitionTo(BookingStatus.SEATED)) {
            links.add(linkTo(methodOn(BookingController.class)
                    .seat(response.id())).withRel("seat"));
        }

        if (response.status().canTransitionTo(BookingStatus.CANCELLED)) {
            links.add(linkTo(methodOn(BookingController.class)
                    .cancel(response.id())).withRel("cancel"));
        }

        return links;
    }
}
