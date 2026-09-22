package se.edugrade.java25.restaurant.booking;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookingModelAssembler implements
        RepresentationModelAssembler<BookingResponse, EntityModel<BookingResponse>> {

    @Override
    public EntityModel<BookingResponse> toModel(BookingResponse response) {
        EntityModel<BookingResponse> model = EntityModel.of(response);

        if (response.status().canTransitionTo(BookingStatus.CONFIRMED)) {
            model.add(linkTo(methodOn(BookingController.class)
                    .confirm(response.id())).withRel("confirm"));
        }

        if (response.status().canTransitionTo(BookingStatus.SEATED)) {
            model.add(linkTo(methodOn(BookingController.class)
                    .seat(response.id())).withRel("seat"));
        }

        if (response.status().canTransitionTo(BookingStatus.CANCELLED)) {
            model.add(linkTo(methodOn(BookingController.class)
                    .cancel(response.id())).withRel("cancel"));
        }

        return model;
    }
}
