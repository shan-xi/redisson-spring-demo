package com.example.stock.api;

import com.example.stock.purchase.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(RuntimeException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler({InsufficientStockException.class, IdempotencyKeyConflictException.class, StaleFencingTokenException.class}) @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(RuntimeException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(ProductBusyException.class) @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> busy(RuntimeException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(RuntimeException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(PurchaseInterruptedException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> interrupted(RuntimeException e) { return Map.of("error", e.getMessage()); }
}
