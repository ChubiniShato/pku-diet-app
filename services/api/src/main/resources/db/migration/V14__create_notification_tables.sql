-- V14__create_notification_tables.sql
-- Create notification system tables

-- Create notification_preference table for user notification settings
CREATE TABLE "notification_preference" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "notification_type" TEXT NOT NULL CHECK ("notification_type" IN (
        'MENU_GENERATED', 'LIMIT_BREACH', 'SUBMISSION_STATUS', 'SHARE_LINK_USED',
        'PANTRY_EXPIRY', 'BUDGET_WARNING', 'MEAL_REMINDER', 'DAILY_SUMMARY'
    )),
    "channel" TEXT NOT NULL CHECK ("channel" IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    "is_enabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "frequency" TEXT CHECK ("frequency" IN ('IMMEDIATE', 'DAILY', 'WEEKLY', 'NEVER')),
    "quiet_hours_start" TIME,
    "quiet_hours_end" TIME,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_patient_type_channel" UNIQUE ("patient_id", "notification_type", "channel")
);

-- Create notification table for storing notification instances
CREATE TABLE "notification" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "notification_type" TEXT NOT NULL CHECK ("notification_type" IN (
        'MENU_GENERATED', 'LIMIT_BREACH', 'SUBMISSION_STATUS', 'SHARE_LINK_USED',
        'PANTRY_EXPIRY', 'BUDGET_WARNING', 'MEAL_REMINDER', 'DAILY_SUMMARY'
    )),
    "title" TEXT NOT NULL,
    "message" TEXT NOT NULL,
    "priority" TEXT NOT NULL CHECK ("priority" IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')) DEFAULT 'NORMAL',
    "status" TEXT NOT NULL CHECK ("status" IN ('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED')) DEFAULT 'PENDING',
    "channels_attempted" TEXT[], -- Array of channels tried
    "channels_successful" TEXT[], -- Array of channels that succeeded
    "entity_type" TEXT, -- e.g., 'menu_day', 'share_link'
    "entity_id" UUID, -- ID of related entity
    "metadata" JSONB, -- Additional context data
    "scheduled_for" TIMESTAMP WITH TIME ZONE,
    "sent_at" TIMESTAMP WITH TIME ZONE,
    "read_at" TIMESTAMP WITH TIME ZONE,
    "expires_at" TIMESTAMP WITH TIME ZONE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_delivery table for tracking delivery attempts
CREATE TABLE "notification_delivery" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "notification_id" UUID NOT NULL REFERENCES "notification"("id") ON DELETE CASCADE,
    "channel" TEXT NOT NULL CHECK ("channel" IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    "recipient" TEXT NOT NULL, -- email address, phone number, etc.
    "status" TEXT NOT NULL CHECK ("status" IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    "attempt_count" INTEGER NOT NULL DEFAULT 0,
    "last_attempt_at" TIMESTAMP WITH TIME ZONE,
    "delivered_at" TIMESTAMP WITH TIME ZONE,
    "error_message" TEXT,
    "provider_id" TEXT, -- External provider message ID
    "provider_response" JSONB,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_template table for message templates
CREATE TABLE "notification_template" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "notification_type" TEXT NOT NULL,
    "channel" TEXT NOT NULL CHECK ("channel" IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    "language" TEXT NOT NULL DEFAULT 'EN',
    "subject_template" TEXT, -- For email
    "body_template" TEXT NOT NULL,
    "variables" TEXT[], -- Available template variables
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "version" INTEGER NOT NULL DEFAULT 1,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_template_version" UNIQUE ("notification_type", "channel", "language", "version")
);

-- Create notification_event table for event sourcing
CREATE TABLE "notification_event" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "event_type" TEXT NOT NULL,
    "event_data" JSONB NOT NULL,
    "entity_type" TEXT,
    "entity_id" UUID,
    "processed" BOOLEAN NOT NULL DEFAULT FALSE,
    "processed_at" TIMESTAMP WITH TIME ZONE,
    "retry_count" INTEGER NOT NULL DEFAULT 0,
    "next_retry_at" TIMESTAMP WITH TIME ZONE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_notification_preference_updated_at
    BEFORE UPDATE ON "notification_preference"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_updated_at
    BEFORE UPDATE ON "notification"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_delivery_updated_at
    BEFORE UPDATE ON "notification_delivery"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_template_updated_at
    BEFORE UPDATE ON "notification_template"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_notification_preference_patient_id ON "notification_preference"("patient_id");
CREATE INDEX IF NOT EXISTS idx_notification_preference_type ON "notification_preference"("notification_type");
CREATE INDEX IF NOT EXISTS idx_notification_preference_enabled ON "notification_preference"("is_enabled");

CREATE INDEX IF NOT EXISTS idx_notification_patient_id ON "notification"("patient_id");
CREATE INDEX IF NOT EXISTS idx_notification_type ON "notification"("notification_type");
CREATE INDEX IF NOT EXISTS idx_notification_status ON "notification"("status");
CREATE INDEX IF NOT EXISTS idx_notification_priority ON "notification"("priority");
CREATE INDEX IF NOT EXISTS idx_notification_scheduled ON "notification"("scheduled_for");
CREATE INDEX IF NOT EXISTS idx_notification_entity ON "notification"("entity_type", "entity_id");

CREATE INDEX IF NOT EXISTS idx_notification_delivery_notification_id ON "notification_delivery"("notification_id");
CREATE INDEX IF NOT EXISTS idx_notification_delivery_channel ON "notification_delivery"("channel");
CREATE INDEX IF NOT EXISTS idx_notification_delivery_status ON "notification_delivery"("status");
CREATE INDEX IF NOT EXISTS idx_notification_delivery_recipient ON "notification_delivery"("recipient");

CREATE INDEX IF NOT EXISTS idx_notification_template_type_channel ON "notification_template"("notification_type", "channel");
CREATE INDEX IF NOT EXISTS idx_notification_template_language ON "notification_template"("language");
CREATE INDEX IF NOT EXISTS idx_notification_template_active ON "notification_template"("is_active");

CREATE INDEX IF NOT EXISTS idx_notification_event_patient_id ON "notification_event"("patient_id");
CREATE INDEX IF NOT EXISTS idx_notification_event_type ON "notification_event"("event_type");
CREATE INDEX IF NOT EXISTS idx_notification_event_processed ON "notification_event"("processed");
CREATE INDEX IF NOT EXISTS idx_notification_event_retry ON "notification_event"("next_retry_at") WHERE "processed" = FALSE;

-- Insert default notification preferences for common types
INSERT INTO "notification_template" ("notification_type", "channel", "language", "subject_template", "body_template", "variables") VALUES
('MENU_GENERATED', 'EMAIL', 'EN', 'Your menu for {{date}} is ready', 'Hello {{patient_name}}, your PKU menu for {{date}} has been generated with {{total_phe}}mg PHE and {{total_kcal}} calories.', ARRAY['patient_name', 'date', 'total_phe', 'total_kcal']),
('LIMIT_BREACH', 'EMAIL', 'EN', 'PHE limit warning for {{date}}', 'Warning: Your PHE intake for {{date}} is {{actual_phe}}mg, which exceeds your daily limit of {{limit_phe}}mg.', ARRAY['patient_name', 'date', 'actual_phe', 'limit_phe']),
('PANTRY_EXPIRY', 'EMAIL', 'EN', 'Items expiring in your pantry', 'The following items in your pantry are expiring soon: {{expiring_items}}', ARRAY['patient_name', 'expiring_items']),
('MEAL_REMINDER', 'PUSH', 'EN', NULL, 'Time for {{meal_name}}! {{phe_amount}}mg PHE planned.', ARRAY['patient_name', 'meal_name', 'phe_amount']);


