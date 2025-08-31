package com.chubini.pku.labelscan;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for LabelScanSubmission entities */
@Repository
public interface LabelScanSubmissionRepository extends JpaRepository<LabelScanSubmission, UUID> {

  /** Find submissions by patient ID */
  List<LabelScanSubmission> findByPatientId(UUID patientId);

  /** Find submissions by status */
  List<LabelScanSubmission> findByStatus(LabelScanSubmission.ScanStatus status);

  /** Find submissions by patient ID and status */
  List<LabelScanSubmission> findByPatientIdAndStatus(
      UUID patientId, LabelScanSubmission.ScanStatus status);
}
