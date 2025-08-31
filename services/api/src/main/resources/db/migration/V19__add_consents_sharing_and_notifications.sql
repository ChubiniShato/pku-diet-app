-- V19__add_consents_sharing_and_notifications.sql
-- Phase 3: Add consent management, secure sharing, and notification support

-- Create patient_consent table
CREATE TABLE IF NOT EXISTS "patient_consent" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "consent_type" TEXT NOT NULL CHECK ("consent_type" IN ('GLOBAL_SUBMISSION_OPTIN', 'SHARE_WITH_DOCTOR', 'EMERGENCY_ACCESS', 'RESEARCH_OPTIN')),
    "status" TEXT NOT NULL CHECK ("status" IN ('GRANTED', 'REVOKED', 'EXPIRED')),
    "version" INTEGER NOT NULL,
    "granted_reason" TEXT,
    "revoked_reason" TEXT,
    "granted_at" TIMESTAMP WITH TIME ZONE NOT NULL,
    "revoked_at" TIMESTAMP WITH TIME ZONE,
    "expires_at" TIMESTAMP WITH TIME ZONE,
    "granted_by" TEXT,
    "revoked_by" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE("patient_id", "consent_type", "version")
);

-- Create share_link table
CREATE TABLE IF NOT EXISTS "share_link" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "token" TEXT NOT NULL UNIQUE,
    "doctor_email" TEXT,
    "doctor_name" TEXT,
    "one_time_use" BOOLEAN NOT NULL DEFAULT TRUE,
    "device_bound" TEXT,
    "ttl_hours" INTEGER NOT NULL DEFAULT 48,
    "expires_at" TIMESTAMP WITH TIME ZONE NOT NULL,
    "status" TEXT NOT NULL DEFAULT 'ACTIVE' CHECK ("status" IN ('ACTIVE', 'USED', 'REVOKED', 'EXPIRED')),
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "revoked_at" TIMESTAMP WITH TIME ZONE,
    "first_used_at" TIMESTAMP WITH TIME ZONE,
    "last_used_at" TIMESTAMP WITH TIME ZONE,
    "usage_count" INTEGER NOT NULL DEFAULT 0,
    "created_by" TEXT,
    "revoked_by" TEXT,
    "notes" TEXT,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create share_link_scopes table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS "share_link_scopes" (
    "share_link_id" UUID NOT NULL REFERENCES "share_link"("id") ON DELETE CASCADE,
    "scope" TEXT NOT NULL CHECK ("scope" IN ('CRITICAL_FACTS', 'DAY', 'WEEK', 'RANGE', 'NUTRITION_SUMMARY')),
    PRIMARY KEY ("share_link_id", "scope")
);

-- Create share_link_access_log table
CREATE TABLE IF NOT EXISTS "share_link_access_log" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "share_link_id" UUID NOT NULL REFERENCES "share_link"("id") ON DELETE CASCADE,
    "access_type" TEXT NOT NULL,
    "resource_accessed" TEXT,
    "client_ip" TEXT,
    "user_agent" TEXT,
    "device_fingerprint" TEXT,
    "geolocation" TEXT,
    "success" BOOLEAN NOT NULL,
    "error_message" TEXT,
    "response_time_ms" BIGINT,
    "accessed_at" TIMESTAMP WITH TIME ZONE NOT NULL,
    "additional_data" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS "idx_patient_consent_patient_id" ON "patient_consent"("patient_id");
CREATE INDEX IF NOT EXISTS "idx_patient_consent_type_status" ON "patient_consent"("consent_type", "status");
CREATE INDEX IF NOT EXISTS "idx_patient_consent_expires_at" ON "patient_consent"("expires_at");

CREATE INDEX IF NOT EXISTS "idx_share_link_token" ON "share_link"("token");
CREATE INDEX IF NOT EXISTS "idx_share_link_patient_id" ON "share_link"("patient_id");
CREATE INDEX IF NOT EXISTS "idx_share_link_doctor_email" ON "share_link"("doctor_email");
CREATE INDEX IF NOT EXISTS "idx_share_link_status_expires" ON "share_link"("status", "expires_at");

CREATE INDEX IF NOT EXISTS "idx_access_log_share_link" ON "share_link_access_log"("share_link_id");
CREATE INDEX IF NOT EXISTS "idx_access_log_timestamp" ON "share_link_access_log"("accessed_at");
CREATE INDEX IF NOT EXISTS "idx_access_log_ip" ON "share_link_access_log"("client_ip");
CREATE INDEX IF NOT EXISTS "idx_access_log_user_agent" ON "share_link_access_log"("user_agent");

-- Add some sample data for testing (optional - can be removed in production)
-- Sample consents for demo patient
-- INSERT INTO "patient_consent" ("patient_id", "consent_type", "status", "version", "granted_reason", "granted_at", "granted_by")
-- SELECT
--     (SELECT "id" FROM "patient_profile" LIMIT 1),
--     'GLOBAL_SUBMISSION_OPTIN',
--     'GRANTED',
--     1,
--     'Initial setup consent',
--     CURRENT_TIMESTAMP,
--     'system'
-- WHERE EXISTS (SELECT 1 FROM "patient_profile" LIMIT 1);

-- Sample share link (requires a patient to exist)
-- INSERT INTO "share_link" ("patient_id", "token", "doctor_email", "doctor_name", "expires_at", "created_by")
-- SELECT
--     (SELECT "id" FROM "patient_profile" LIMIT 1),
--     'sample-token-12345',
--     'doctor@example.com',
--     'Dr. Smith',
--     CURRENT_TIMESTAMP + INTERVAL '48 hours',
--     'patient-app'
-- WHERE EXISTS (SELECT 1 FROM "patient_profile" LIMIT 1);

-- Sample share link scopes
-- INSERT INTO "share_link_scopes" ("share_link_id", "scope")
-- SELECT sl."id", 'CRITICAL_FACTS'
-- FROM "share_link" sl
-- WHERE sl."token" = 'sample-token-12345';

-- INSERT INTO "share_link_scopes" ("share_link_id", "scope")
-- SELECT sl."id", 'DAY'
-- FROM "share_link" sl
-- WHERE sl."token" = 'sample-token-12345';

-- Add comments for documentation
COMMENT ON TABLE "patient_consent" IS 'Patient consent records for data sharing and submissions';
COMMENT ON TABLE "share_link" IS 'Secure share links for doctor-patient data sharing';
COMMENT ON TABLE "share_link_scopes" IS 'Scopes associated with share links';
COMMENT ON TABLE "share_link_access_log" IS 'Audit log for share link access attempts';

-- Create a view for active consents (optional)
CREATE OR REPLACE VIEW "active_patient_consents" AS
SELECT pc.*
FROM "patient_consent" pc
WHERE pc."status" = 'GRANTED'
  AND (pc."expires_at" IS NULL OR pc."expires_at" > CURRENT_TIMESTAMP);

-- Create a view for active share links (optional)
CREATE OR REPLACE VIEW "active_share_links" AS
SELECT sl.*
FROM "share_link" sl
WHERE sl."status" = 'ACTIVE'
  AND sl."expires_at" > CURRENT_TIMESTAMP;
