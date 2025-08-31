package com.chubini.pku.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.chubini.pku.notifications.providers.FcmPushProvider;
import com.chubini.pku.notifications.providers.SendGridEmailProvider;
import com.chubini.pku.notifications.providers.TwilioSmsProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private FcmPushProvider pushProvider;

  @Mock private SendGridEmailProvider emailProvider;

  @Mock private TwilioSmsProvider smsProvider;

  @InjectMocks private NotificationService notificationService;

  private UUID testPatientId;
  private String testPatientName;

  @BeforeEach
  void setUp() {
    testPatientId = UUID.randomUUID();
    testPatientName = "Test Patient";
  }

  @Test
  void testSendNotification_SingleChannel_Success() {
    // Given
    NotificationMessage.BreachNotification message =
        new NotificationMessage.BreachNotification(
            testPatientId, testPatientName, "PHE", 10.5, "HIGH");
    Set<NotificationType> preferences = Set.of(NotificationType.PUSH);

    when(pushProvider.isAvailable()).thenReturn(true);
    when(pushProvider.send(message))
        .thenReturn(new NotificationProvider.NotificationResult(true, "MSG-123", null));

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendNotification(message, preferences);

    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getMessageId()).isEqualTo("MSG-123");

    verify(pushProvider).send(message);
    verify(emailProvider, never()).send(any());
    verify(smsProvider, never()).send(any());
  }

  @Test
  void testSendNotification_MultipleChannels_Success() {
    // Given
    NotificationMessage.BreachNotification message =
        new NotificationMessage.BreachNotification(
            testPatientId, testPatientName, "PHE", 10.5, "HIGH");
    Set<NotificationType> preferences = Set.of(NotificationType.PUSH, NotificationType.EMAIL);

    when(pushProvider.isAvailable()).thenReturn(true);
    when(emailProvider.isAvailable()).thenReturn(true);
    when(pushProvider.send(message))
        .thenReturn(new NotificationProvider.NotificationResult(true, "PUSH-123", null));
    when(emailProvider.send(message))
        .thenReturn(new NotificationProvider.NotificationResult(true, "EMAIL-456", null));

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendNotification(message, preferences);

    // Then
    assertThat(result.isSuccess()).isTrue();

    verify(pushProvider).send(message);
    verify(emailProvider).send(message);
    verify(smsProvider, never()).send(any());
  }

  @Test
  void testSendNotification_NoChannelsAvailable_Failure() {
    // Given
    NotificationMessage.BreachNotification message =
        new NotificationMessage.BreachNotification(
            testPatientId, testPatientName, "PHE", 10.5, "HIGH");
    Set<NotificationType> preferences = Set.of(NotificationType.PUSH);

    when(pushProvider.isAvailable()).thenReturn(false);

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendNotification(message, preferences);

    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrorMessage()).isEqualTo("No providers available");

    verify(pushProvider, never()).send(any());
  }

  @Test
  void testSendBreachNotification_Success() {
    // Given
    Set<NotificationType> preferences = Set.of(NotificationType.PUSH);

    when(pushProvider.isAvailable()).thenReturn(true);
    when(pushProvider.send(any()))
        .thenReturn(new NotificationProvider.NotificationResult(true, "BREACH-123", null));

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendBreachNotification(
            testPatientId, testPatientName, "PHE", 10.5, "HIGH", preferences);

    // Then
    assertThat(result.isSuccess()).isTrue();

    verify(pushProvider).send(any(NotificationMessage.BreachNotification.class));
  }

  @Test
  void testSendSuggestionsNotification_Success() {
    // Given
    Set<NotificationType> preferences = Set.of(NotificationType.EMAIL);

    when(emailProvider.isAvailable()).thenReturn(true);
    when(emailProvider.send(any()))
        .thenReturn(new NotificationProvider.NotificationResult(true, "SUGGESTIONS-123", null));

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendSuggestionsNotification(
            testPatientId, testPatientName, 3, "snack", preferences);

    // Then
    assertThat(result.isSuccess()).isTrue();

    verify(emailProvider).send(any(NotificationMessage.SuggestionsNotification.class));
  }

  @Test
  void testSendDoctorInvitationEmail_Success() {
    // Given
    String doctorEmail = "doctor@example.com";
    String doctorName = "Dr. Smith";
    String patientName = "John Doe";
    String shareLinkUrl = "https://app.example.com/share/abc123";
    String expiryInfo = "48 hours";

    when(emailProvider.isAvailable()).thenReturn(true);
    when(emailProvider.sendSecureInvitationEmail(
            doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo))
        .thenReturn(new NotificationProvider.NotificationResult(true, "INVITE-123", null));

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendDoctorInvitationEmail(
            doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);

    // Then
    assertThat(result.isSuccess()).isTrue();

    verify(emailProvider)
        .sendSecureInvitationEmail(doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);
  }

  @Test
  void testSendDoctorInvitationEmail_ProviderNotAvailable_LogsOnly() {
    // Given
    String doctorEmail = "doctor@example.com";
    String doctorName = "Dr. Smith";
    String patientName = "John Doe";
    String shareLinkUrl = "https://app.example.com/share/abc123";
    String expiryInfo = "48 hours";

    when(emailProvider.isAvailable()).thenReturn(false);

    // When
    NotificationProvider.NotificationResult result =
        notificationService.sendDoctorInvitationEmail(
            doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);

    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getMessageId()).startsWith("LOGGED-");

    verify(emailProvider)
        .sendSecureInvitationEmail(doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);
  }

  @Test
  void testGetProviderStatus_ReturnsAllStatuses() {
    // Given
    when(pushProvider.isAvailable()).thenReturn(true);
    when(emailProvider.isAvailable()).thenReturn(false);
    when(smsProvider.isAvailable()).thenReturn(true);

    // When
    Map<NotificationType, Boolean> status = notificationService.getProviderStatus();

    // Then
    assertThat(status).hasSize(3);
    assertThat(status.get(NotificationType.PUSH)).isTrue();
    assertThat(status.get(NotificationType.EMAIL)).isFalse();
    assertThat(status.get(NotificationType.SMS)).isTrue();
  }

  @Test
  void testSendTestNotifications_AllChannels() {
    // Given
    when(pushProvider.isAvailable()).thenReturn(true);
    when(emailProvider.isAvailable()).thenReturn(true);
    when(smsProvider.isAvailable()).thenReturn(false);

    when(pushProvider.send(any()))
        .thenReturn(new NotificationProvider.NotificationResult(true, "TEST-PUSH", null));
    when(emailProvider.send(any()))
        .thenReturn(new NotificationProvider.NotificationResult(false, null, "Email failed"));

    // When
    Map<NotificationType, NotificationProvider.NotificationResult> results =
        notificationService.sendTestNotifications(testPatientId, testPatientName);

    // Then
    assertThat(results).hasSize(3);
    assertThat(results.get(NotificationType.PUSH).isSuccess()).isTrue();
    assertThat(results.get(NotificationType.EMAIL).isSuccess()).isFalse();
    assertThat(results.get(NotificationType.SMS)).isNull(); // Not available
  }
}
