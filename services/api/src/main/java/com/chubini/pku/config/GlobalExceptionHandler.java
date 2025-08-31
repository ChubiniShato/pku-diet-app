package com.chubini.pku.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.chubini.pku.products.ProductNotFoundException;
import com.chubini.pku.products.ProductUploadException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<Object> handleProductNotFound(
      ProductNotFoundException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Product Not Found");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ProductUploadException.class)
  public ResponseEntity<Object> handleProductUpload(ProductUploadException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Product Upload Error");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation Failed");
    body.put("path", request.getDescription(false));

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    body.put("validationErrors", errors);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Object> handleBadCredentials(
      BadCredentialsException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.UNAUTHORIZED.value());
    body.put("error", "Authentication Failed");
    body.put("message", "Invalid username or password");
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Object> handleUsernameNotFound(
      UsernameNotFoundException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.UNAUTHORIZED.value());
    body.put("error", "Authentication Failed");
    body.put("message", "Invalid username or password");
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Object> handleMaxSizeException(
      MaxUploadSizeExceededException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
    body.put("error", "File Too Large");
    body.put("message", "File size exceeds maximum allowed limit");
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation Error");
    body.put("path", request.getDescription(false));

    // Extract constraint violations
    Map<String, String> violations =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing + "; " + replacement));

    body.put("message", "Request validation failed");
    body.put("validationErrors", violations);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", "An unexpected error occurred");
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
