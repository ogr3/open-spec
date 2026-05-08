package com.openspec.usernameservice.api;

import com.openspec.usernameservice.service.HandleAllocationService;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("invalid_request", "Invalid request payload", details));
    }

    @ExceptionHandler(HandleAllocationService.HandleAllocationException.class)
    public ResponseEntity<ErrorResponse> handleAllocation(HandleAllocationService.HandleAllocationException ex) {
        HttpStatus status =
                switch (ex.getCode()) {
                    case "all_blocked" -> HttpStatus.UNPROCESSABLE_ENTITY;
                    case "email_already_reserved" -> HttpStatus.CONFLICT;
                    case "collisions_exhausted" -> HttpStatus.CONFLICT;
                    default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };
        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), List.of(ex.getEmail())));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        "persistence_error", "Unable to persist reservation", List.of(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("invalid_request", ex.getMessage(), List.of()));
    }
}
