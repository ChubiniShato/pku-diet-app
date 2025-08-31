package com.chubini.pku.sharing;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** REST controller for secure access to shared patient data via tokens */
@RestController
@RequestMapping("/api/v1/shared")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Shared Data Access",
    description = "Secure endpoints for accessing shared patient data via tokens")
public class SharedDataController {

  private final ShareLinkService shareLinkService;

  // TODO: Inject services for accessing different types of data
  // private final MenuDayRepository menuDayRepository;
  // private final CriticalFactRepository criticalFactRepository;
  // private final NutritionCalculator nutritionCalculator;

  @Operation(
      summary = "Access shared data",
      description = "Access patient data using a secure share link token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Data accessed successfully",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "403", description = "Access not allowed for this token"),
        @ApiResponse(responseCode = "404", description = "Requested data not found"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
      })
  @GetMapping("/data")
  public ResponseEntity<SharedDataResponse> accessSharedData(
      @Parameter(description = "Share link token", required = true) @RequestParam String token,
      @Parameter(description = "Type of data to access", example = "CRITICAL_FACTS")
          @RequestParam(defaultValue = "CRITICAL_FACTS")
          String dataType,
      @Parameter(description = "Date range start (for RANGE scope)", example = "2024-01-01")
          @RequestParam(required = false)
          String startDate,
      @Parameter(description = "Date range end (for RANGE scope)", example = "2024-01-07")
          @RequestParam(required = false)
          String endDate,
      @Parameter(description = "Specific date (for DAY scope)", example = "2024-01-15")
          @RequestParam(required = false)
          String date,
      @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
      @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
      @RequestHeader(value = "X-Real-IP", required = false) String realIp) {

    String clientIp = forwardedFor != null ? forwardedFor : realIp;
    String accessType = "VIEW";
    String resourceAccessed = buildResourceString(dataType, startDate, endDate, date);

    log.info("Token access attempt from IP: {} for resource: {}", clientIp, resourceAccessed);

    // Validate token and access
    ShareLinkService.ShareLinkAccessResult accessResult =
        shareLinkService.validateAndAccessShareLink(
            token, accessType, resourceAccessed, clientIp, userAgent, 0L);

    if (!accessResult.isSuccess()) {
      log.warn("Token access denied: {}", accessResult.getErrorMessage());
      return ResponseEntity.status(401).build();
    }

    try {
      // Get the shared data based on the request
      SharedDataResponse response =
          getSharedData(accessResult.getShareLink(), dataType, startDate, endDate, date);

      log.info("Successfully provided shared data for token: {}", maskToken(token));
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving shared data", e);
      return ResponseEntity.status(500).build();
    }
  }

  @Operation(
      summary = "Get share link metadata",
      description = "Get metadata about a share link without accessing data")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkMetadata.class))),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "404", description = "Share link not found")
      })
  @GetMapping("/metadata")
  public ResponseEntity<ShareLinkMetadata> getShareLinkMetadata(
      @Parameter(description = "Share link token", required = true) @RequestParam String token) {

    log.debug("Retrieving share link metadata for token");

    try {
      ShareLink shareLink =
          shareLinkService
              .getShareLinkByToken(token)
              .orElseThrow(() -> new ShareLinkService.ShareLinkNotFoundException("Invalid token"));

      ShareLinkMetadata metadata =
          new ShareLinkMetadata(
              shareLink.getPatient().getName(),
              shareLink.getDoctorName(),
              shareLink.getScopes().stream().map(Enum::toString).toList(),
              shareLink.getExpiresAt(),
              shareLink.getUsageCount(),
              shareLink.isUsable());

      return ResponseEntity.ok(metadata);

    } catch (ShareLinkService.ShareLinkNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving share link metadata", e);
      return ResponseEntity.status(500).build();
    }
  }

  /** Get shared data based on the request parameters */
  private SharedDataResponse getSharedData(
      ShareLink shareLink, String dataType, String startDate, String endDate, String date) {

    // TODO: Implement actual data retrieval based on scopes and data type
    // This is a placeholder implementation

    List<Map<String, Object>> data = List.of();

    switch (dataType.toUpperCase()) {
      case "CRITICAL_FACTS":
        if (shareLink.allowsScope(ShareLink.ShareScope.CRITICAL_FACTS)) {
          data = getCriticalFactsData(shareLink);
        }
        break;

      case "DAY":
        if (shareLink.allowsScope(ShareLink.ShareScope.DAY) && date != null) {
          data = getDayData(shareLink, date);
        }
        break;

      case "WEEK":
        if (shareLink.allowsScope(ShareLink.ShareScope.WEEK)) {
          data = getWeekData(shareLink);
        }
        break;

      case "RANGE":
        if (shareLink.allowsScope(ShareLink.ShareScope.RANGE)
            && startDate != null
            && endDate != null) {
          data = getRangeData(shareLink, startDate, endDate);
        }
        break;

      case "NUTRITION_SUMMARY":
        if (shareLink.allowsScope(ShareLink.ShareScope.NUTRITION_SUMMARY)) {
          data = getNutritionSummaryData(shareLink);
        }
        break;

      default:
        throw new IllegalArgumentException("Unsupported data type: " + dataType);
    }

    return new SharedDataResponse(
        dataType,
        shareLink.getPatient().getName(),
        data,
        "Data retrieved successfully via secure share link",
        shareLink.getExpiresAt());
  }

  // Placeholder implementations - replace with actual data access
  private List<Map<String, Object>> getCriticalFactsData(ShareLink shareLink) {
    // TODO: Implement critical facts retrieval
    return List.of(
        Map.of(
            "type", "CRITICAL_FACT",
            "message", "Sample critical fact data",
            "timestamp", java.time.LocalDateTime.now().toString()));
  }

  private List<Map<String, Object>> getDayData(ShareLink shareLink, String date) {
    // TODO: Implement day data retrieval
    return List.of(
        Map.of("date", date, "type", "DAY_DATA", "message", "Sample day data for " + date));
  }

  private List<Map<String, Object>> getWeekData(ShareLink shareLink) {
    // TODO: Implement week data retrieval
    return List.of(
        Map.of(
            "type", "WEEK_DATA",
            "message", "Sample week data"));
  }

  private List<Map<String, Object>> getRangeData(
      ShareLink shareLink, String startDate, String endDate) {
    // TODO: Implement range data retrieval
    return List.of(
        Map.of(
            "startDate",
            startDate,
            "endDate",
            endDate,
            "type",
            "RANGE_DATA",
            "message",
            "Sample range data from " + startDate + " to " + endDate));
  }

  private List<Map<String, Object>> getNutritionSummaryData(ShareLink shareLink) {
    // TODO: Implement nutrition summary retrieval
    return List.of(
        Map.of(
            "type", "NUTRITION_SUMMARY",
            "message", "Sample nutrition summary data"));
  }

  private String buildResourceString(
      String dataType, String startDate, String endDate, String date) {
    return switch (dataType.toUpperCase()) {
      case "DAY" -> "DAY:" + (date != null ? date : "unknown");
      case "WEEK" -> "WEEK";
      case "RANGE" -> "RANGE:" + startDate + "-" + endDate;
      case "CRITICAL_FACTS" -> "CRITICAL_FACTS";
      case "NUTRITION_SUMMARY" -> "NUTRITION_SUMMARY";
      default -> dataType;
    };
  }

  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return token;
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
  }

  /** Response wrapper for shared data */
  public record SharedDataResponse(
      String dataType,
      String patientName,
      List<Map<String, Object>> data,
      String message,
      java.time.LocalDateTime expiresAt) {}

  /** Response for share link metadata */
  public record ShareLinkMetadata(
      String patientName,
      String doctorName,
      List<String> scopes,
      java.time.LocalDateTime expiresAt,
      Integer usageCount,
      boolean isActive) {}
}
