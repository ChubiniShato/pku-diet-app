package com.chubini.pku.moderation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for ModerationSubmission entities */
@Repository
public interface ModerationSubmissionRepository extends JpaRepository<ModerationSubmission, UUID> {

  /** Find submissions by status */
  List<ModerationSubmission> findByStatus(ModerationSubmission.ModerationStatus status);

  /** Find submissions by target type */
  List<ModerationSubmission> findByTargetType(ModerationSubmission.TargetType targetType);

  /** Find submissions by status and target type */
  List<ModerationSubmission> findByStatusAndTargetType(
      ModerationSubmission.ModerationStatus status, ModerationSubmission.TargetType targetType);

  /** Find submissions by submitter */
  List<ModerationSubmission> findBySubmittedBy(String submittedBy);
}
