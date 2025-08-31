package com.chubini.pku.patients;

/** Exception thrown when a patient profile is not found */
public class PatientNotFoundException extends RuntimeException {

  public PatientNotFoundException(String message) {
    super(message);
  }

  public PatientNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
