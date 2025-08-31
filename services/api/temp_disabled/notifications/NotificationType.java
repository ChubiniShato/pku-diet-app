package com.chubini.pku.notifications;

/** Enumeration of notification types */
public enum NotificationType {
  EMAIL,
  SMS,
  PUSH,
  IN_APP,
  BREACH,
  SUGGESTIONS,
  CONSENT_EXPIRY,
  SHARE_LINK_CREATED,
  SUBMISSION_STATUS,
  LABEL_SCAN;

  /** Check if notification type requires external provider */
  public boolean requiresExternalProvider() {
    return switch (this) {
      case EMAIL -> true;
      case SMS -> true;
      case PUSH -> true;
      case IN_APP,
              BREACH,
              SUGGESTIONS,
              CONSENT_EXPIRY,
              SHARE_LINK_CREATED,
              SUBMISSION_STATUS,
              LABEL_SCAN ->
          false;
    };
  }

  /** Get human-readable name */
  public String getDisplayName() {
    return switch (this) {
      case EMAIL -> "Email";
      case SMS -> "SMS";
      case PUSH -> "Push Notification";
      case IN_APP -> "In-App Notification";
      case BREACH -> "Nutritional Breach";
      case SUGGESTIONS -> "Diet Suggestions";
      case CONSENT_EXPIRY -> "Consent Expiry";
      case SHARE_LINK_CREATED -> "Share Link Created";
      case SUBMISSION_STATUS -> "Submission Status";
      case LABEL_SCAN -> "Label Scan Results";
    };
  }
}
