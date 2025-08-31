package com.chubini.pku.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientConsentServiceTest {

  @Mock private PatientConsentRepository consentRepository;

  @InjectMocks private PatientConsentService consentService;

  private PatientProfile testPatient;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    testPatient = PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    now = LocalDateTime.now();
  }

  @Test
  void testGrantConsent_NewConsent_Success() {
    // Given
    PatientConsent.ConsentType consentType = PatientConsent.ConsentType.SHARE_WITH_DOCTOR;
    String reason = "Doctor consultation";
    LocalDateTime expiresAt = now.plusDays(30);
    String grantedBy = "patient-app";

    PatientConsent expectedConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .consentType(consentType)
            .status(PatientConsent.ConsentStatus.GRANTED)
            .version(1)
            .grantedReason(reason)
            .grantedAt(now)
            .expiresAt(expiresAt)
            .grantedBy(grantedBy)
            .build();

    when(consentRepository.findActiveConsentByPatientAndType(
            eq(testPatient), eq(consentType), any()))
        .thenReturn(Optional.empty());
    when(consentRepository.findLatestConsentByPatientAndType(testPatient, consentType))
        .thenReturn(Optional.empty());
    when(consentRepository.save(any(PatientConsent.class))).thenReturn(expectedConsent);

    // When
    PatientConsent result =
        consentService.grantConsent(testPatient, consentType, reason, expiresAt, grantedBy);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getConsentType()).isEqualTo(consentType);
    assertThat(result.getStatus()).isEqualTo(PatientConsent.ConsentStatus.GRANTED);
    assertThat(result.getVersion()).isEqualTo(1);
    assertThat(result.getGrantedReason()).isEqualTo(reason);

    verify(consentRepository).save(any(PatientConsent.class));
  }

  @Test
  void testGrantConsent_ExistingActiveConsent_Replaced() {
    // Given
    PatientConsent.ConsentType consentType = PatientConsent.ConsentType.SHARE_WITH_DOCTOR;

    PatientConsent existingConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .consentType(consentType)
            .status(PatientConsent.ConsentStatus.GRANTED)
            .version(1)
            .build();

    PatientConsent newConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .consentType(consentType)
            .status(PatientConsent.ConsentStatus.GRANTED)
            .version(2)
            .build();

    when(consentRepository.findActiveConsentByPatientAndType(
            eq(testPatient), eq(consentType), any()))
        .thenReturn(Optional.of(existingConsent));
    when(consentRepository.findLatestConsentByPatientAndType(testPatient, consentType))
        .thenReturn(Optional.of(existingConsent));
    when(consentRepository.save(any(PatientConsent.class))).thenReturn(newConsent);

    // When
    PatientConsent result =
        consentService.grantConsent(testPatient, consentType, "New reason", null, "user");

    // Then
    assertThat(result.getVersion()).isEqualTo(2);

    // Verify existing consent was revoked
    ArgumentCaptor<PatientConsent> captor = ArgumentCaptor.forClass(PatientConsent.class);
    verify(consentRepository, times(2)).save(captor.capture());

    List<PatientConsent> savedConsents = captor.getAllValues();
    assertThat(savedConsents.get(0).getStatus()).isEqualTo(PatientConsent.ConsentStatus.REVOKED);
    assertThat(savedConsents.get(1).getVersion()).isEqualTo(2);
  }

  @Test
  void testRevokeConsent_ValidConsent_Success() {
    // Given
    UUID consentId = UUID.randomUUID();
    String reason = "No longer needed";
    String revokedBy = "patient-app";

    PatientConsent consent =
        PatientConsent.builder()
            .id(consentId)
            .patient(testPatient)
            .consentType(PatientConsent.ConsentType.SHARE_WITH_DOCTOR)
            .status(PatientConsent.ConsentStatus.GRANTED)
            .version(1)
            .build();

    when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));
    when(consentRepository.save(any(PatientConsent.class))).thenReturn(consent);

    // When
    PatientConsent result = consentService.revokeConsent(consentId, reason, revokedBy);

    // Then
    assertThat(result.getStatus()).isEqualTo(PatientConsent.ConsentStatus.REVOKED);
    assertThat(result.getRevokedReason()).isEqualTo(reason);
    assertThat(result.getRevokedBy()).isEqualTo(revokedBy);
    assertThat(result.getRevokedAt()).isNotNull();

    verify(consentRepository).save(consent);
  }

  @Test
  void testRevokeConsent_ConsentNotFound_ThrowsException() {
    // Given
    UUID consentId = UUID.randomUUID();
    when(consentRepository.findById(consentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> consentService.revokeConsent(consentId, "reason", "user"))
        .isInstanceOf(PatientConsentService.ConsentNotFoundException.class)
        .hasMessage("Consent not found with ID: " + consentId);
  }

  @Test
  void testRevokeConsent_AlreadyRevoked_ThrowsException() {
    // Given
    UUID consentId = UUID.randomUUID();
    PatientConsent consent =
        PatientConsent.builder().id(consentId).status(PatientConsent.ConsentStatus.REVOKED).build();

    when(consentRepository.findById(consentId)).thenReturn(Optional.of(consent));

    // When & Then
    assertThatThrownBy(() -> consentService.revokeConsent(consentId, "reason", "user"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Consent is not currently granted");
  }

  @Test
  void testHasActiveConsent_ActiveConsent_ReturnsTrue() {
    // Given
    PatientConsent.ConsentType consentType = PatientConsent.ConsentType.SHARE_WITH_DOCTOR;

    PatientConsent activeConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .status(PatientConsent.ConsentStatus.GRANTED)
            .expiresAt(now.plusDays(30))
            .build();

    when(consentRepository.findActiveConsentByPatientAndType(
            eq(testPatient), eq(consentType), any()))
        .thenReturn(Optional.of(activeConsent));

    // When
    boolean hasConsent = consentService.hasActiveConsent(testPatient, consentType);

    // Then
    assertThat(hasConsent).isTrue();
  }

  @Test
  void testHasActiveConsent_NoActiveConsent_ReturnsFalse() {
    // Given
    PatientConsent.ConsentType consentType = PatientConsent.ConsentType.SHARE_WITH_DOCTOR;

    when(consentRepository.findActiveConsentByPatientAndType(
            eq(testPatient), eq(consentType), any()))
        .thenReturn(Optional.empty());

    // When
    boolean hasConsent = consentService.hasActiveConsent(testPatient, consentType);

    // Then
    assertThat(hasConsent).isFalse();
  }

  @Test
  void testGetActivePatientConsents_ReturnsActiveConsents() {
    // Given
    PatientConsent activeConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .status(PatientConsent.ConsentStatus.GRANTED)
            .build();

    when(consentRepository.findActiveConsentsByPatient(eq(testPatient), any()))
        .thenReturn(List.of(activeConsent));

    // When
    List<PatientConsent> activeConsents = consentService.getActivePatientConsents(testPatient);

    // Then
    assertThat(activeConsents).hasSize(1);
    assertThat(activeConsents.get(0)).isEqualTo(activeConsent);
  }

  @Test
  void testProcessExpiredConsents_UpdatesExpiredConsents() {
    // Given
    PatientConsent expiredConsent =
        PatientConsent.builder()
            .id(UUID.randomUUID())
            .status(PatientConsent.ConsentStatus.GRANTED)
            .expiresAt(now.minusDays(1))
            .build();

    when(consentRepository.findExpiredConsents(any())).thenReturn(List.of(expiredConsent));
    when(consentRepository.save(any(PatientConsent.class))).thenReturn(expiredConsent);

    // When
    consentService.processExpiredConsents();

    // Then
    ArgumentCaptor<PatientConsent> captor = ArgumentCaptor.forClass(PatientConsent.class);
    verify(consentRepository).save(captor.capture());

    PatientConsent saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(PatientConsent.ConsentStatus.EXPIRED);
  }
}
