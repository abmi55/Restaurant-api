package se.edugrade.java25.restaurant.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import se.edugrade.java25.restaurant.booking.BookingNotFoundException;
import se.edugrade.java25.restaurant.booking.InvalidBookingStateException;
import se.edugrade.java25.restaurant.restaurant.RestaurantNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({RestaurantNotFoundException.class, BookingNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException exception) {
        return problemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    ProblemDetail handleInvalidBookingState(InvalidBookingStateException exception) {
        return problemDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    // Thrown when ?sort= names a field the entity does not have, e.g. sort=string from Swagger UI.
    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail handleInvalidSortProperty(PropertyReferenceException exception) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Invalid sort property: " + exception.getPropertyName());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return ResponseEntity.status(status)
                .body(problemDetail(HttpStatus.BAD_REQUEST, "Request validation failed"));
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Unexpected error while handling {} {}", request.getMethod(), request.getRequestURI(), exception);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ProblemDetail problemDetail(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
