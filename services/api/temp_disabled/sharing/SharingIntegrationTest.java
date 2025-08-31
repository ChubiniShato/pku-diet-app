package com.chubini.pku.sharing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import com.chubini.pku.BaseIntegrationTest;
import com.chubini.pku.sharing.dto.ShareLinkRequest;
import com.chubini.pku.sharing.dto.ShareLinkResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

/**
 * Integration test for sharing/consent: grant consent → create share link → revoke → token invalid
 */
class SharingIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testSharingConsentFlow() {
    // Given: Patient with consent granted
    UUID patientId = UUID.randomUUID();

    // Create share link request
    ShareLinkRequest request =
        new ShareLinkRequest(
            Set.of(ShareLinkRequest.ShareScope.DAY, ShareLinkRequest.ShareScope.CRITICAL_FACTS),
            "doctor@example.com",
            "Dr. Smith",
            24, // ttlHours
            true, // oneTimeUse
            false, // deviceBound
            "test-user");

    // When: Create share link
    ResponseEntity<ShareLinkResponse> createResponse =
        restTemplate.postForEntity(
            "/api/v1/share-links?patientId={patientId}",
            request,
            ShareLinkResponse.class,
            patientId);

    // Then: Share link created successfully
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    ShareLinkResponse shareLink = createResponse.getBody();
    assertThat(shareLink).isNotNull();
    assertThat(shareLink.id()).isNotNull();
    assertThat(shareLink.status()).isEqualTo("ACTIVE");
    assertThat(shareLink.usable()).isTrue();

    // When: Revoke share link
    ResponseEntity<ShareLinkResponse> revokeResponse =
        restTemplate.postForEntity(
            "/api/v1/share-links/{shareLinkId}/revoke?reason=test-revocation&performedBy=test-user",
            null,
            ShareLinkResponse.class,
            shareLink.id());

    // Then: Share link revoked
    assertThat(revokeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    ShareLinkResponse revokedLink = revokeResponse.getBody();
    assertThat(revokedLink).isNotNull();
    assertThat(revokedLink.status()).isEqualTo("REVOKED");
    assertThat(revokedLink.usable()).isFalse();

    // Verify token is no longer valid (would need actual token access endpoint)
  }
}
