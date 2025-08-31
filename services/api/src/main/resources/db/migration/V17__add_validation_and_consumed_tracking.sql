-- V17__add_validation_and_consumed_tracking.sql
-- Add consumed quantity tracking and critical facts table

-- Add consumed_qty column to menu_entry table
ALTER TABLE "menu_entry" 
ADD COLUMN "consumed_qty" NUMERIC(8,2);

-- Create critical_fact table for tracking nutrition breaches
CREATE TABLE "critical_fact" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "menu_day_id" UUID NOT NULL REFERENCES "menu_day"("id") ON DELETE CASCADE,
    "breach_type" TEXT NOT NULL CHECK ("breach_type" IN ('PHE_EXCEEDED', 'PROTEIN_EXCEEDED', 'KCAL_DEFICIT', 'FAT_EXCEEDED')),
    "delta_value" NUMERIC(10,2) NOT NULL,
    "limit_value" NUMERIC(10,2),
    "actual_value" NUMERIC(10,2),
    "context_type" TEXT NOT NULL CHECK ("context_type" IN ('planned', 'consumed')),
    "description" TEXT NOT NULL,
    "severity" TEXT NOT NULL CHECK ("severity" IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    "resolved" BOOLEAN NOT NULL DEFAULT FALSE,
    "resolved_at" TIMESTAMP WITH TIME ZONE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for critical_fact table
CREATE INDEX IF NOT EXISTS idx_critical_fact_patient_id ON "critical_fact"("patient_id");
CREATE INDEX IF NOT EXISTS idx_critical_fact_menu_day_id ON "critical_fact"("menu_day_id");
CREATE INDEX IF NOT EXISTS idx_critical_fact_breach_type ON "critical_fact"("breach_type");
CREATE INDEX IF NOT EXISTS idx_critical_fact_severity ON "critical_fact"("severity");
CREATE INDEX IF NOT EXISTS idx_critical_fact_resolved ON "critical_fact"("resolved");
CREATE INDEX IF NOT EXISTS idx_critical_fact_created_at ON "critical_fact"("created_at");

-- Create trigger for critical_fact updated_at
CREATE TRIGGER update_critical_fact_updated_at
    BEFORE UPDATE ON "critical_fact"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comment to consumed_qty column
COMMENT ON COLUMN "menu_entry"."consumed_qty" IS 'Actual quantity consumed by the patient (separate from planned quantity)';

-- Add comments to critical_fact table
COMMENT ON TABLE "critical_fact" IS 'Records critical nutritional breaches that require attention';
COMMENT ON COLUMN "critical_fact"."breach_type" IS 'Type of nutritional breach (PHE/protein exceeded, calories deficit, etc.)';
COMMENT ON COLUMN "critical_fact"."delta_value" IS 'Amount by which the limit was exceeded or deficit occurred';
COMMENT ON COLUMN "critical_fact"."context_type" IS 'Whether breach occurred in planned or consumed values';
COMMENT ON COLUMN "critical_fact"."severity" IS 'Severity level based on percentage of breach';
