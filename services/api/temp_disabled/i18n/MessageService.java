package com.chubini.pku.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for localized message resolution */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

  private final MessageSource messageSource;

  // Supported locales
  public static final Locale ENGLISH = Locale.ENGLISH;
  public static final Locale GEORGIAN = new Locale("ka");
  public static final Locale RUSSIAN = new Locale("ru");

  /** Get localized message by key */
  public String getMessage(String key, Locale locale) {
    return getMessage(key, null, locale);
  }

  /** Get localized message by key with parameters */
  public String getMessage(String key, Object[] args, Locale locale) {
    try {
      return messageSource.getMessage(key, args, locale);
    } catch (NoSuchMessageException e) {
      log.warn(
          "Message not found for key '{}' in locale '{}', trying English fallback", key, locale);

      // Try English fallback
      try {
        return messageSource.getMessage(key, args, ENGLISH);
      } catch (NoSuchMessageException e2) {
        log.error("Message not found for key '{}' even in English fallback", key);
        return "Message not available: " + key;
      }
    }
  }

  /** Get localized message with default locale (English) */
  public String getMessage(String key) {
    return getMessage(key, ENGLISH);
  }

  /** Get localized message with parameters and default locale */
  public String getMessage(String key, Object[] args) {
    return getMessage(key, args, ENGLISH);
  }

  /** Resolve locale from string */
  public Locale resolveLocale(String localeStr) {
    if (localeStr == null || localeStr.trim().isEmpty()) {
      return ENGLISH;
    }

    String lowerLocale = localeStr.toLowerCase();

    switch (lowerLocale) {
      case "ka":
      case "kat":
      case "georgian":
        return GEORGIAN;
      case "ru":
      case "rus":
      case "russian":
        return RUSSIAN;
      case "en":
      case "eng":
      case "english":
      default:
        return ENGLISH;
    }
  }

  /** Check if locale is supported */
  public boolean isSupportedLocale(Locale locale) {
    return locale.equals(ENGLISH) || locale.equals(GEORGIAN) || locale.equals(RUSSIAN);
  }

  /** Get all supported locales */
  public Locale[] getSupportedLocales() {
    return new Locale[] {ENGLISH, GEORGIAN, RUSSIAN};
  }

  /** Get default locale */
  public Locale getDefaultLocale() {
    return ENGLISH;
  }

  /** Get localized validation message */
  public String getValidationMessage(String key, Locale locale) {
    return getMessage("validation." + key, locale);
  }

  /** Get localized validation message with parameters */
  public String getValidationMessage(String key, Object[] args, Locale locale) {
    return getMessage("validation." + key, args, locale);
  }

  /** Get localized label scan message */
  public String getLabelScanMessage(String key, Locale locale) {
    return getMessage("labelscan." + key, locale);
  }

  /** Get localized label scan message with parameters */
  public String getLabelScanMessage(String key, Object[] args, Locale locale) {
    return getMessage("labelscan." + key, args, locale);
  }

  /** Get localized moderation message */
  public String getModerationMessage(String key, Locale locale) {
    return getMessage("moderation." + key, locale);
  }

  /** Get localized moderation message with parameters */
  public String getModerationMessage(String key, Object[] args, Locale locale) {
    return getMessage("moderation." + key, args, locale);
  }

  /** Get localized notification message */
  public String getNotificationMessage(String key, Locale locale) {
    return getMessage("notification." + key, locale);
  }

  /** Get localized notification message with parameters */
  public String getNotificationMessage(String key, Object[] args, Locale locale) {
    return getMessage("notification." + key, args, locale);
  }

  /** Get localized security message */
  public String getSecurityMessage(String key, Locale locale) {
    return getMessage("security." + key, locale);
  }

  /** Get localized security message with parameters */
  public String getSecurityMessage(String key, Object[] args, Locale locale) {
    return getMessage("security." + key, args, locale);
  }

  /** Get localized general message */
  public String getGeneralMessage(String key, Locale locale) {
    return getMessage("general." + key, locale);
  }

  /** Get localized general message with parameters */
  public String getGeneralMessage(String key, Object[] args, Locale locale) {
    return getMessage("general." + key, args, locale);
  }

  /** Get localized menu generation message */
  public String getMenuGenerationMessage(String key, Locale locale) {
    return getMessage("menu.generation." + key, locale);
  }

  /** Get localized menu generation message with parameters */
  public String getMenuGenerationMessage(String key, Object[] args, Locale locale) {
    return getMessage("menu.generation." + key, args, locale);
  }

  /** Get localized barcode lookup message */
  public String getBarcodeLookupMessage(String key, Locale locale) {
    return getMessage("barcode.lookup." + key, locale);
  }

  /** Get localized barcode lookup message with parameters */
  public String getBarcodeLookupMessage(String key, Object[] args, Locale locale) {
    return getMessage("barcode.lookup." + key, args, locale);
  }

  /** Get localized OCR message */
  public String getOcrMessage(String key, Locale locale) {
    return getMessage("ocr." + key, locale);
  }

  /** Get localized OCR message with parameters */
  public String getOcrMessage(String key, Object[] args, Locale locale) {
    return getMessage("ocr." + key, args, locale);
  }
}
