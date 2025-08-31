-- V20__add_label_scans_and_moderation.sql
-- Phase 4: Add label scanning and moderation support

-- Create label_scan_submission table
CREATE TABLE IF NOT EXISTS "label_scan_submission" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "barcode" TEXT,
    "region" VARCHAR(10),
    "status" TEXT NOT NULL DEFAULT 'PENDING' CHECK ("status" IN ('PENDING', 'PROCESSING', 'MATCHED', 'REQUIRES_REVIEW', 'APPROVED', 'REJECTED', 'FAILED')),
    "ocr_text" TEXT,
    "ocr_confidence" DECIMAL(5,4),
    "matched_product_id" UUID REFERENCES "products"("id") ON DELETE SET NULL,
    "match_confidence" DECIMAL(5,4),
    "match_source" TEXT CHECK ("match_source" IN ('BARCODE', 'OCR_NAME', 'MANUAL')),
    "review_notes" TEXT,
    "reviewed_by" TEXT,
    "reviewed_at" TIMESTAMP WITH TIME ZONE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "processed_at" TIMESTAMP WITH TIME ZONE,
    "error_message" TEXT
);

-- Create label_scan_images table (many-to-many with images)
CREATE TABLE IF NOT EXISTS "label_scan_images" (
    "submission_id" UUID NOT NULL REFERENCES "label_scan_submission"("id") ON DELETE CASCADE,
    "image_path" TEXT NOT NULL,
    PRIMARY KEY ("submission_id", "image_path")
);

-- Create label_scan_extracted_fields table
CREATE TABLE IF NOT EXISTS "label_scan_extracted_fields" (
    "submission_id" UUID NOT NULL REFERENCES "label_scan_submission"("id") ON DELETE CASCADE,
    "field_name" TEXT NOT NULL,
    "field_value" TEXT,
    PRIMARY KEY ("submission_id", "field_name")
);

-- Create label_scan_allergen_hits table
CREATE TABLE IF NOT EXISTS "label_scan_allergen_hits" (
    "submission_id" UUID NOT NULL REFERENCES "label_scan_submission"("id") ON DELETE CASCADE,
    "allergen" TEXT NOT NULL,
    PRIMARY KEY ("submission_id", "allergen")
);

-- Create label_scan_forbidden_hits table
CREATE TABLE IF NOT EXISTS "label_scan_forbidden_hits" (
    "submission_id" UUID NOT NULL REFERENCES "label_scan_submission"("id") ON DELETE CASCADE,
    "ingredient" TEXT NOT NULL,
    PRIMARY KEY ("submission_id", "ingredient")
);

-- Create label_scan_warnings table
CREATE TABLE IF NOT EXISTS "label_scan_warnings" (
    "submission_id" UUID NOT NULL REFERENCES "label_scan_submission"("id") ON DELETE CASCADE,
    "warning" TEXT NOT NULL,
    PRIMARY KEY ("submission_id", "warning")
);

-- Create moderation_submission table
CREATE TABLE IF NOT EXISTS "moderation_submission" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "target_type" TEXT NOT NULL CHECK ("target_type" IN ('CUSTOM_PRODUCT', 'CUSTOM_DISH', 'LABEL_SCAN')),
    "payload_ref" TEXT NOT NULL,
    "status" TEXT NOT NULL DEFAULT 'DRAFT' CHECK ("status" IN ('DRAFT', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'EXPIRED')),
    "submission_title" TEXT,
    "submission_description" TEXT,
    "review_notes" TEXT,
    "reviewer_id" TEXT,
    "reviewed_at" TIMESTAMP WITH TIME ZONE,
    "approved_product_id" UUID,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "expires_at" TIMESTAMP WITH TIME ZONE
);

-- Create moderation_conflicts table
CREATE TABLE IF NOT EXISTS "moderation_conflicts" (
    "submission_id" UUID NOT NULL REFERENCES "moderation_submission"("id") ON DELETE CASCADE,
    "conflict_description" TEXT NOT NULL,
    PRIMARY KEY ("submission_id", "conflict_description")
);

-- Create moderation_merge_data table
CREATE TABLE IF NOT EXISTS "moderation_merge_data" (
    "submission_id" UUID NOT NULL REFERENCES "moderation_submission"("id") ON DELETE CASCADE,
    "field_path" TEXT NOT NULL,
    "field_value" TEXT,
    PRIMARY KEY ("submission_id", "field_path")
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS "idx_label_scan_patient_id" ON "label_scan_submission"("patient_id");
CREATE INDEX IF NOT EXISTS "idx_label_scan_status" ON "label_scan_submission"("status");
CREATE INDEX IF NOT EXISTS "idx_label_scan_barcode" ON "label_scan_submission"("barcode");
CREATE INDEX IF NOT EXISTS "idx_label_scan_created_at" ON "label_scan_submission"("created_at");
CREATE INDEX IF NOT EXISTS "idx_label_scan_matched_product" ON "label_scan_submission"("matched_product_id");

CREATE INDEX IF NOT EXISTS "idx_moderation_patient_id" ON "moderation_submission"("patient_id");
CREATE INDEX IF NOT EXISTS "idx_moderation_status" ON "moderation_submission"("status");
CREATE INDEX IF NOT EXISTS "idx_moderation_target_type" ON "moderation_submission"("target_type");
CREATE INDEX IF NOT EXISTS "idx_moderation_created_at" ON "moderation_submission"("created_at");
CREATE INDEX IF NOT EXISTS "idx_moderation_expires_at" ON "moderation_submission"("expires_at");

-- Add sample data for testing (optional - can be removed in production)
-- Sample label scan submission
-- INSERT INTO "label_scan_submission" ("patient_id", "barcode", "region", "status", "ocr_text", "ocr_confidence")
-- SELECT
--     (SELECT "id" FROM "patient_profile" LIMIT 1),
--     '123456789012',
--     'US',
--     'MATCHED',
--     'INGREDIENTS: Water, Sugar, Artificial Flavors',
--     0.85
-- WHERE EXISTS (SELECT 1 FROM "patient_profile" LIMIT 1);

-- Sample label scan images
-- INSERT INTO "label_scan_images" ("submission_id", "image_path")
-- SELECT lss."id", 'data/label-scans/sample1.jpg'
-- FROM "label_scan_submission" lss
-- WHERE lss."barcode" = '123456789012';

-- Sample moderation submission
-- INSERT INTO "moderation_submission" ("patient_id", "target_type", "payload_ref", "status", "submission_title")
-- SELECT
--     (SELECT "id" FROM "patient_profile" LIMIT 1),
--     'CUSTOM_PRODUCT',
--     'custom_product:12345678-1234-1234-1234-123456789012',
--     'PENDING',
--     'Organic Apple Sauce'
-- WHERE EXISTS (SELECT 1 FROM "patient_profile" LIMIT 1);

-- Add comments for documentation
COMMENT ON TABLE "label_scan_submission" IS 'Food label scan submissions for OCR processing and safety analysis';
COMMENT ON TABLE "label_scan_images" IS 'Images associated with label scan submissions';
COMMENT ON TABLE "label_scan_extracted_fields" IS 'Structured fields extracted from label scans';
COMMENT ON TABLE "label_scan_allergen_hits" IS 'Allergens detected in label scans';
COMMENT ON TABLE "label_scan_forbidden_hits" IS 'Forbidden ingredients detected in label scans';
COMMENT ON TABLE "label_scan_warnings" IS 'General warnings from label scan processing';
COMMENT ON TABLE "moderation_submission" IS 'Submissions for global catalog moderation';
COMMENT ON TABLE "moderation_conflicts" IS 'Conflicts detected during moderation';
COMMENT ON TABLE "moderation_merge_data" IS 'Proposed field changes for moderation approval';

-- Create views for common queries (optional)
CREATE OR REPLACE VIEW "active_label_scans" AS
SELECT lss.*
FROM "label_scan_submission" lss
WHERE lss."status" IN ('PENDING', 'PROCESSING', 'REQUIRES_REVIEW');

CREATE OR REPLACE VIEW "pending_moderations" AS
SELECT ms.*
FROM "moderation_submission" ms
WHERE ms."status" IN ('PENDING', 'UNDER_REVIEW')
  AND (ms."expires_at" IS NULL OR ms."expires_at" > CURRENT_TIMESTAMP);

CREATE OR REPLACE VIEW "moderation_queue_summary" AS
SELECT
    COUNT(*) as total_pending,
    COUNT(CASE WHEN target_type = 'CUSTOM_PRODUCT' THEN 1 END) as custom_products,
    COUNT(CASE WHEN target_type = 'CUSTOM_DISH' THEN 1 END) as custom_dishes,
    COUNT(CASE WHEN target_type = 'LABEL_SCAN' THEN 1 END) as label_scans,
    MIN(created_at) as oldest_submission,
    MAX(created_at) as newest_submission
FROM "moderation_submission"
WHERE "status" IN ('PENDING', 'UNDER_REVIEW')
  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);
