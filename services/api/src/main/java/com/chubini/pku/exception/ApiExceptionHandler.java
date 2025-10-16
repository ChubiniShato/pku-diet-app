package com.chubini.pku.exception;

import java.time.LocalDateTime;

import com.chubini.pku.metrics.ErrorMetrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/** Global exception handler with metrics integration and cardinality control. */
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {
  private final ErrorMetrics errorMetrics;

  @Value("${pku.errors.db.as503:true}")
  private boolean dbAsServiceUnavailable;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleAny(HttpServletRequest req, Exception ex) {
    String pattern = getEndpointPattern(req);
    errorMetrics.record(pattern, ex.getClass().getSimpleName());

    ApiError apiError =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(req.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiError> handleDataAccess(HttpServletRequest req, DataAccessException ex) {
    String pattern = getEndpointPattern(req);
    errorMetrics.record(pattern, "database_error");

    HttpStatus status =
        dbAsServiceUnavailable ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;

    ApiError apiError =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message("Database operation failed")
            .path(req.getRequestURI())
            .build();

    return ResponseEntity.status(status).body(apiError);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleValidation(
      HttpServletRequest req, IllegalArgumentException ex) {
    String pattern = getEndpointPattern(req);
    errorMetrics.record(pattern, "validation_error");

    ApiError apiError =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(req.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(apiError);
  }

  /**
   * Gets the endpoint pattern from the request to avoid high cardinality.
   *
   * @param req the HTTP request
   * @return the endpoint pattern (e.g., "/products/{id}") or "unknown"
   */
  private String getEndpointPattern(HttpServletRequest req) {
    String pattern = (String) req.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return pattern != null ? pattern : "unknown";
  }

  /** API Error response structure. */
  public static class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public static ApiErrorBuilder builder() {
      return new ApiErrorBuilder();
    }

    public static class ApiErrorBuilder {
      private LocalDateTime timestamp;
      private int status;
      private String error;
      private String message;
      private String path;

      public ApiErrorBuilder timestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
      }

      public ApiErrorBuilder status(int status) {
        this.status = status;
        return this;
      }

      public ApiErrorBuilder error(String error) {
        this.error = error;
        return this;
      }

      public ApiErrorBuilder message(String message) {
        this.message = message;
        return this;
      }

      public ApiErrorBuilder path(String path) {
        this.path = path;
        return this;
      }

      public ApiError build() {
        ApiError apiError = new ApiError();
        apiError.timestamp = this.timestamp;
        apiError.status = this.status;
        apiError.error = this.error;
        apiError.message = this.message;
        apiError.path = this.path;
        return apiError;
      }
    }

    // Getters
    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getError() {
      return error;
    }

    public String getMessage() {
      return message;
    }

    public String getPath() {
      return path;
    }
  }
}
