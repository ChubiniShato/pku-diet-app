package com.chubini.pku.labelscan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import com.chubini.pku.BaseIntegrationTest;
import com.chubini.pku.labelscan.dto.LabelScanResponse;
import com.chubini.pku.labelscan.dto.LabelScanStatusResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Integration test for label scan: POST label-scan (mock OCR + mock OpenFoodFacts) → flags →
 * moderation approve
 */
class LabelScanIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testLabelScanFlow() {
    // Given: Patient and test image
    UUID patientId = UUID.randomUUID();

    // Prepare multipart request with test image
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("images", new ClassPathResource("test-food-label.jpg"));
    body.add("region", "US");
    body.add("barcode", "123456789012");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    // When: Upload images for scanning
    ResponseEntity<LabelScanResponse> uploadResponse =
        restTemplate.postForEntity(
            "/api/v1/label-scan?patientId={patientId}",
            requestEntity,
            LabelScanResponse.class,
            patientId);

    // Then: Scan submission created
    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    LabelScanResponse scanResponse = uploadResponse.getBody();
    assertThat(scanResponse).isNotNull();
    assertThat(scanResponse.submissionId()).isNotNull();

    // When: Get scan status
    ResponseEntity<LabelScanStatusResponse> statusResponse =
        restTemplate.getForEntity(
            "/api/v1/label-scan/{submissionId}?patientId={patientId}",
            LabelScanStatusResponse.class,
            scanResponse.submissionId(),
            patientId);

    // Then: Status retrieved
    assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    LabelScanStatusResponse status = statusResponse.getBody();
    assertThat(status).isNotNull();
    assertThat(status.submissionId()).isEqualTo(scanResponse.submissionId());

    // Verify flags and moderation workflow would continue here
    // (would need actual moderation endpoints and test data)
  }
}
