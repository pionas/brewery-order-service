package pl.excellentapp.brewery.order.infrastructure.rest.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.excellentapp.brewery.order.domain.exception.OrderNotFoundException;
import pl.excellentapp.brewery.order.utils.DateTimeProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@Component
@AllArgsConstructor
public class GlobalExceptionHandler {

    private DateTimeProvider dateTimeProvider;

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handle(OrderNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return handleError(HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        return handleError(HttpStatus.BAD_REQUEST, errors);
    }

    private ResponseEntity<Object> handleError(HttpStatus status, Object errors) {
        final var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", dateTimeProvider.now());
        body.put("status", status.value());
        body.put("errors", errors);
        return new ResponseEntity<>(body, status);
    }
}
