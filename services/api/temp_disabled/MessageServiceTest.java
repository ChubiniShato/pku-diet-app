package com.chubini.pku.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock private MessageSource messageSource;

  private MessageService messageService;

  @BeforeEach
  void setUp() {
    messageService = new MessageService(messageSource);
  }

  @Test
  void testGetMessage_Success() {
    // Given
    when(messageSource.getMessage("test.key", null, Locale.ENGLISH)).thenReturn("Test Message");

    // When
    String result = messageService.getMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Test Message");
  }

  @Test
  void testGetMessage_WithParameters_Success() {
    // Given
    Object[] args = new Object[] {"param1", "param2"};
    when(messageSource.getMessage("test.key", args, Locale.ENGLISH))
        .thenReturn("Test Message with param1 and param2");

    // When
    String result = messageService.getMessage("test.key", args, Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Test Message with param1 and param2");
  }

  @Test
  void testGetMessage_FallbackToEnglish_Success() {
    // Given
    when(messageSource.getMessage("test.key", null, Locale.GERMAN))
        .thenThrow(NoSuchMessageException.class);
    when(messageSource.getMessage("test.key", null, Locale.ENGLISH))
        .thenReturn("English Fallback Message");

    // When
    String result = messageService.getMessage("test.key", Locale.GERMAN);

    // Then
    assertThat(result).isEqualTo("English Fallback Message");
  }

  @Test
  void testGetMessage_NoMessageAvailable_ReturnsFallback() {
    // Given
    when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenThrow(NoSuchMessageException.class);

    // When
    String result = messageService.getMessage("missing.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Message not available: missing.key");
  }

  @Test
  void testResolveLocale_SupportedLocales() {
    // When & Then
    assertThat(messageService.resolveLocale("en")).isEqualTo(Locale.ENGLISH);
    assertThat(messageService.resolveLocale("english")).isEqualTo(Locale.ENGLISH);
    assertThat(messageService.resolveLocale("ka")).isEqualTo(new Locale("ka"));
    assertThat(messageService.resolveLocale("georgian")).isEqualTo(new Locale("ka"));
    assertThat(messageService.resolveLocale("ru")).isEqualTo(new Locale("ru"));
    assertThat(messageService.resolveLocale("russian")).isEqualTo(new Locale("ru"));
  }

  @Test
  void testResolveLocale_UnsupportedLocale_DefaultsToEnglish() {
    // When
    Locale result = messageService.resolveLocale("fr");

    // Then
    assertThat(result).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void testResolveLocale_NullOrEmpty_DefaultsToEnglish() {
    // When & Then
    assertThat(messageService.resolveLocale(null)).isEqualTo(Locale.ENGLISH);
    assertThat(messageService.resolveLocale("")).isEqualTo(Locale.ENGLISH);
    assertThat(messageService.resolveLocale("   ")).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void testIsSupportedLocale() {
    // When & Then
    assertThat(messageService.isSupportedLocale(Locale.ENGLISH)).isTrue();
    assertThat(messageService.isSupportedLocale(new Locale("ka"))).isTrue();
    assertThat(messageService.isSupportedLocale(new Locale("ru"))).isTrue();
    assertThat(messageService.isSupportedLocale(Locale.GERMAN)).isFalse();
  }

  @Test
  void testGetSupportedLocales() {
    // When
    Locale[] supportedLocales = messageService.getSupportedLocales();

    // Then
    assertThat(supportedLocales).hasSize(3);
    assertThat(supportedLocales).contains(Locale.ENGLISH, new Locale("ka"), new Locale("ru"));
  }

  @Test
  void testGetDefaultLocale() {
    // When
    Locale defaultLocale = messageService.getDefaultLocale();

    // Then
    assertThat(defaultLocale).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void testGetValidationMessage() {
    // Given
    when(messageSource.getMessage("validation.test.key", null, Locale.ENGLISH))
        .thenReturn("Validation message");

    // When
    String result = messageService.getValidationMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Validation message");
    verify(messageSource).getMessage("validation.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetLabelScanMessage() {
    // Given
    when(messageSource.getMessage("labelscan.test.key", null, Locale.ENGLISH))
        .thenReturn("Label scan message");

    // When
    String result = messageService.getLabelScanMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Label scan message");
    verify(messageSource).getMessage("labelscan.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetModerationMessage() {
    // Given
    when(messageSource.getMessage("moderation.test.key", null, Locale.ENGLISH))
        .thenReturn("Moderation message");

    // When
    String result = messageService.getModerationMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Moderation message");
    verify(messageSource).getMessage("moderation.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetNotificationMessage() {
    // Given
    when(messageSource.getMessage("notification.test.key", null, Locale.ENGLISH))
        .thenReturn("Notification message");

    // When
    String result = messageService.getNotificationMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Notification message");
    verify(messageSource).getMessage("notification.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetSecurityMessage() {
    // Given
    when(messageSource.getMessage("security.test.key", null, Locale.ENGLISH))
        .thenReturn("Security message");

    // When
    String result = messageService.getSecurityMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Security message");
    verify(messageSource).getMessage("security.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetGeneralMessage() {
    // Given
    when(messageSource.getMessage("general.test.key", null, Locale.ENGLISH))
        .thenReturn("General message");

    // When
    String result = messageService.getGeneralMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("General message");
    verify(messageSource).getMessage("general.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetMenuGenerationMessage() {
    // Given
    when(messageSource.getMessage("menu.generation.test.key", null, Locale.ENGLISH))
        .thenReturn("Menu generation message");

    // When
    String result = messageService.getMenuGenerationMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Menu generation message");
    verify(messageSource).getMessage("menu.generation.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetBarcodeLookupMessage() {
    // Given
    when(messageSource.getMessage("barcode.lookup.test.key", null, Locale.ENGLISH))
        .thenReturn("Barcode lookup message");

    // When
    String result = messageService.getBarcodeLookupMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("Barcode lookup message");
    verify(messageSource).getMessage("barcode.lookup.test.key", null, Locale.ENGLISH);
  }

  @Test
  void testGetOcrMessage() {
    // Given
    when(messageSource.getMessage("ocr.test.key", null, Locale.ENGLISH)).thenReturn("OCR message");

    // When
    String result = messageService.getOcrMessage("test.key", Locale.ENGLISH);

    // Then
    assertThat(result).isEqualTo("OCR message");
    verify(messageSource).getMessage("ocr.test.key", null, Locale.ENGLISH);
  }
}
