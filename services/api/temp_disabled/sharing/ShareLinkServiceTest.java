package com.chubini.pku.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.chubini.pku.consents.PatientConsent;
import com.chubini.pku.consents.PatientConsentService;
import com.chubini.pku.patients.PatientProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareLinkServiceTest {

  @Mock private ShareLinkRepository shareLinkRepository;

  @Mock private ShareLinkAccessLogRepository accessLogRepository;

  @Mock private PatientConsentService consentService;

  @InjectMocks private ShareLinkService shareLinkService;

  private PatientProfile testPatient;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    testPatient = PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    now = LocalDateTime.now();
  }

  @Test
  void testCreateShareLink_WithValidConsent_Success() {
    // Given
    Set<ShareLink.ShareScope> scopes = Set.of(ShareLink.ShareScope.CRITICAL_FACTS);
    String doctorEmail = "doctor@example.com";
    String doctorName = "Dr. Smith";
    Integer ttlHours = 48;
    Boolean oneTimeUse = true;
    String createdBy = "patient-app";

    ShareLink expectedShareLink =
        ShareLink.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .token("test-token-123")
            .doctorEmail(doctorEmail)
            .doctorName(doctorName)
            .scopes(scopes)
            .oneTimeUse(oneTimeUse)
            .ttlHours(ttlHours)
            .expiresAt(now.plusHours(ttlHours))
            .createdBy(createdBy)
            .build();

    when(consentService.hasActiveConsent(testPatient, PatientConsent.ConsentType.SHARE_WITH_DOCTOR))
        .thenReturn(true);
    when(shareLinkRepository.save(any(ShareLink.class))).thenReturn(expectedShareLink);

    // When
    ShareLink result =
        shareLinkService.createShareLink(
            testPatient, scopes, doctorEmail, doctorName, ttlHours, oneTimeUse, null, createdBy);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPatient()).isEqualTo(testPatient);
    assertThat(result.getDoctorEmail()).isEqualTo(doctorEmail);
    assertThat(result.getDoctorName()).isEqualTo(doctorName);
    assertThat(result.getScopes()).isEqualTo(scopes);
    assertThat(result.getOneTimeUse()).isEqualTo(oneTimeUse);
    assertThat(result.getTtlHours()).isEqualTo(ttlHours);
    assertThat(result.getExpiresAt()).isAfter(now);

    verify(shareLinkRepository).save(any(ShareLink.class));
  }

  @Test
  void testCreateShareLink_WithoutConsent_ThrowsException() {
    // Given
    Set<ShareLink.ShareScope> scopes = Set.of(ShareLink.ShareScope.CRITICAL_FACTS);

    when(consentService.hasActiveConsent(testPatient, PatientConsent.ConsentType.SHARE_WITH_DOCTOR))
        .thenReturn(false);

    // When & Then
    assertThatThrownBy(
            () ->
                shareLinkService.createShareLink(
                    testPatient, scopes, "doctor@example.com", "Dr. Smith", 48, true, null, "user"))
        .isInstanceOf(ShareLinkService.InsufficientConsentException.class)
        .hasMessage("Patient must grant SHARE_WITH_DOCTOR consent before creating share links");
  }

  @Test
  void testValidateAndAccessShareLink_ValidToken_Success() {
    // Given
    String token = "valid-token-123";
    String accessType = "VIEW";
    String resourceAccessed = "CRITICAL_FACTS";
    String clientIp = "192.168.1.1";
    String userAgent = "Test Browser";
    long responseTimeMs = 150L;

    ShareLink shareLink =
        ShareLink.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .token(token)
            .scopes(Set.of(ShareLink.ShareScope.CRITICAL_FACTS))
            .status(ShareLink.ShareLinkStatus.ACTIVE)
            .expiresAt(now.plusDays(1))
            .usageCount(0)
            .oneTimeUse(false)
            .build();

    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(shareLink));
    when(shareLinkRepository.save(any(ShareLink.class))).thenReturn(shareLink);
    when(accessLogRepository.save(any(ShareLinkAccessLog.class)))
        .thenReturn(mock(ShareLinkAccessLog.class));

    // When
    ShareLinkService.ShareLinkAccessResult result =
        shareLinkService.validateAndAccessShareLink(
            token, accessType, resourceAccessed, clientIp, userAgent, responseTimeMs);

    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getShareLink()).isEqualTo(shareLink);

    // Verify usage was recorded
    ArgumentCaptor<ShareLink> linkCaptor = ArgumentCaptor.forClass(ShareLink.class);
    verify(shareLinkRepository).save(linkCaptor.capture());
    assertThat(linkCaptor.getValue().getUsageCount()).isEqualTo(1);
    assertThat(linkCaptor.getValue().getLastUsedAt()).isNotNull();

    // Verify access log was created
    verify(accessLogRepository).save(any(ShareLinkAccessLog.class));
  }

  @Test
  void testValidateAndAccessShareLink_InvalidToken_Failure() {
    // Given
    String token = "invalid-token";
    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.empty());

    // When
    ShareLinkService.ShareLinkAccessResult result =
        shareLinkService.validateAndAccessShareLink(
            token, "VIEW", "CRITICAL_FACTS", "192.168.1.1", "Browser", 100L);

    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Share link not found");

    verify(accessLogRepository)
        .save(any(ShareLinkAccessLog.class)); // Should log the failed attempt
  }

  @Test
  void testValidateAndAccessShareLink_ExpiredToken_Failure() {
    // Given
    String token = "expired-token";
    ShareLink expiredLink =
        ShareLink.builder()
            .id(UUID.randomUUID())
            .token(token)
            .status(ShareLink.ShareLinkStatus.ACTIVE)
            .expiresAt(now.minusDays(1))
            .build();

    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(expiredLink));

    // When
    ShareLinkService.ShareLinkAccessResult result =
        shareLinkService.validateAndAccessShareLink(
            token, "VIEW", "CRITICAL_FACTS", "192.168.1.1", "Browser", 100L);

    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("Share link has expired");
  }

  @Test
  void testValidateAndAccessShareLink_OneTimeTokenUsed_Failure() {
    // Given
    String token = "used-token";
    ShareLink usedLink =
        ShareLink.builder()
            .id(UUID.randomUUID())
            .token(token)
            .status(ShareLink.ShareLinkStatus.USED)
            .oneTimeUse(true)
            .build();

    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(usedLink));

    // When
    ShareLinkService.ShareLinkAccessResult result =
        shareLinkService.validateAndAccessShareLink(
            token, "VIEW", "CRITICAL_FACTS", "192.168.1.1", "Browser", 100L);

    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("One-time share link has already been used");
  }

  @Test
  void testValidateAndAccessShareLink_UnauthorizedScope_Failure() {
    // Given
    String token = "limited-token";
    ShareLink limitedLink =
        ShareLink.builder()
            .id(UUID.randomUUID())
            .token(token)
            .scopes(Set.of(ShareLink.ShareScope.CRITICAL_FACTS)) // Only critical facts
            .status(ShareLink.ShareLinkStatus.ACTIVE)
            .expiresAt(now.plusDays(1))
            .build();

    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(limitedLink));

    // When - trying to access DAY data but only CRITICAL_FACTS is allowed
    ShareLinkService.ShareLinkAccessResult result =
        shareLinkService.validateAndAccessShareLink(
            token, "VIEW", "DAY:2024-01-15", "192.168.1.1", "Browser", 100L);

    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage())
        .isEqualTo("Requested scope not allowed for this share link");
  }

  @Test
  void testRevokeShareLink_ValidLink_Success() {
    // Given
    UUID shareLinkId = UUID.randomUUID();
    String revokedBy = "patient-app";

    ShareLink shareLink =
        ShareLink.builder().id(shareLinkId).status(ShareLink.ShareLinkStatus.ACTIVE).build();

    when(shareLinkRepository.findById(shareLinkId)).thenReturn(Optional.of(shareLink));
    when(shareLinkRepository.save(any(ShareLink.class))).thenReturn(shareLink);

    // When
    ShareLink result = shareLinkService.revokeShareLink(shareLinkId, revokedBy);

    // Then
    assertThat(result.getStatus()).isEqualTo(ShareLink.ShareLinkStatus.REVOKED);
    assertThat(result.getRevokedBy()).isEqualTo(revokedBy);
    assertThat(result.getRevokedAt()).isNotNull();

    verify(shareLinkRepository).save(shareLink);
  }

  @Test
  void testRevokeShareLink_AlreadyRevoked_ThrowsException() {
    // Given
    UUID shareLinkId = UUID.randomUUID();
    ShareLink revokedLink =
        ShareLink.builder().id(shareLinkId).status(ShareLink.ShareLinkStatus.REVOKED).build();

    when(shareLinkRepository.findById(shareLinkId)).thenReturn(Optional.of(revokedLink));

    // When & Then
    assertThatThrownBy(() -> shareLinkService.revokeShareLink(shareLinkId, "user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Share link is already revoked");
  }

  @Test
  void testGetShareLinkByToken_ValidToken_ReturnsLink() {
    // Given
    String token = "valid-token";
    ShareLink shareLink = ShareLink.builder().id(UUID.randomUUID()).token(token).build();

    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(shareLink));

    // When
    Optional<ShareLink> result = shareLinkService.getShareLinkByToken(token);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(shareLink);
  }

  @Test
  void testGetShareLinkByToken_InvalidToken_ReturnsEmpty() {
    // Given
    String token = "invalid-token";
    when(shareLinkRepository.findByToken(token)).thenReturn(Optional.empty());

    // When
    Optional<ShareLink> result = shareLinkService.getShareLinkByToken(token);

    // Then
    assertThat(result).isEmpty();
  }
}
