-- V18__add_pantry_and_pricing_tables.sql
-- Phase 2: Add pantry and pricing support for budget-aware menu generation

-- Create pantry_item table
CREATE TABLE IF NOT EXISTS "pantry_item" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "product_id" UUID REFERENCES "product"("id") ON DELETE CASCADE,
    "custom_product_id" UUID REFERENCES "custom_product"("id") ON DELETE CASCADE,
    "item_type" TEXT NOT NULL CHECK ("item_type" IN ('PRODUCT', 'CUSTOM_PRODUCT')),
    "quantity_grams" NUMERIC(10,2) NOT NULL CHECK ("quantity_grams" >= 0),
    "purchase_date" DATE,
    "expiry_date" DATE,
    "location" TEXT, -- FRIDGE, PANTRY, FREEZER
    "cost_per_unit" NUMERIC(10,2) CHECK ("cost_per_unit" >= 0),
    "currency" TEXT DEFAULT 'USD',
    "notes" TEXT,
    "is_available" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT "pantry_item_product_xor" CHECK (
        ("product_id" IS NOT NULL AND "custom_product_id" IS NULL AND "item_type" = 'PRODUCT') OR
        ("product_id" IS NULL AND "custom_product_id" IS NOT NULL AND "item_type" = 'CUSTOM_PRODUCT')
    )
);

-- Create price_entry table for market pricing
CREATE TABLE IF NOT EXISTS "price_entry" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "product_id" UUID REFERENCES "product"("id") ON DELETE CASCADE,
    "custom_product_id" UUID REFERENCES "custom_product"("id") ON DELETE CASCADE,
    "item_type" TEXT NOT NULL CHECK ("item_type" IN ('PRODUCT', 'CUSTOM_PRODUCT')),
    "store_name" TEXT,
    "region" TEXT,
    "price_per_unit" NUMERIC(10,2) NOT NULL CHECK ("price_per_unit" >= 0),
    "unit_size_grams" NUMERIC(10,2) NOT NULL CHECK ("unit_size_grams" > 0),
    "currency" TEXT NOT NULL DEFAULT 'USD',
    "recorded_date" DATE NOT NULL DEFAULT CURRENT_DATE,
    "is_current" BOOLEAN NOT NULL DEFAULT TRUE,
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT "price_entry_product_xor" CHECK (
        ("product_id" IS NOT NULL AND "custom_product_id" IS NULL AND "item_type" = 'PRODUCT') OR
        ("product_id" IS NULL AND "custom_product_id" IS NOT NULL AND "item_type" = 'CUSTOM_PRODUCT')
    )
);

-- Create indexes for pantry_item
CREATE INDEX IF NOT EXISTS "idx_pantry_item_patient_id" ON "pantry_item"("patient_id");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_product_id" ON "pantry_item"("product_id");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_custom_product_id" ON "pantry_item"("custom_product_id");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_available" ON "pantry_item"("is_available");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_expiry" ON "pantry_item"("expiry_date");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_location" ON "pantry_item"("location");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_patient_product" ON "pantry_item"("patient_id", "product_id", "is_available");
CREATE INDEX IF NOT EXISTS "idx_pantry_item_patient_custom_product" ON "pantry_item"("patient_id", "custom_product_id", "is_available");

-- Create indexes for price_entry
CREATE INDEX IF NOT EXISTS "idx_price_entry_product_id" ON "price_entry"("product_id");
CREATE INDEX IF NOT EXISTS "idx_price_entry_custom_product_id" ON "price_entry"("custom_product_id");
CREATE INDEX IF NOT EXISTS "idx_price_entry_current" ON "price_entry"("is_current");
CREATE INDEX IF NOT EXISTS "idx_price_entry_date" ON "price_entry"("recorded_date");
CREATE INDEX IF NOT EXISTS "idx_price_entry_product_current" ON "price_entry"("product_id", "is_current", "recorded_date");
CREATE INDEX IF NOT EXISTS "idx_price_entry_custom_product_current" ON "price_entry"("custom_product_id", "is_current", "recorded_date");

-- Add some sample data for testing (optional - can be removed in production)
-- Sample pantry items for demo patient (if exists)
-- INSERT INTO "pantry_item" ("patient_id", "product_id", "item_type", "quantity_grams", "location", "cost_per_unit", "currency")
-- SELECT 
--     (SELECT "id" FROM "patient_profile" LIMIT 1),
--     "id",
--     'PRODUCT',
--     500.00,
--     'PANTRY',
--     5.00,
--     'USD'
-- FROM "product" 
-- WHERE "product_name" ILIKE '%rice%' 
-- LIMIT 1;

-- Sample price entries for common products (if they exist)
-- INSERT INTO "price_entry" ("product_id", "item_type", "store_name", "price_per_unit", "unit_size_grams", "currency")
-- SELECT 
--     "id",
--     'PRODUCT',
--     'Demo Store',
--     ROUND((RANDOM() * 10 + 1)::numeric, 2), -- Random price between 1-11
--     500.00, -- 500g packages
--     'USD'
-- FROM "product" 
-- WHERE "category" IN ('vegetables', 'fruits', 'grains')
-- LIMIT 10;

COMMENT ON TABLE "pantry_item" IS 'Patient pantry inventory for pantry-aware menu generation';
COMMENT ON TABLE "price_entry" IS 'Market pricing data for budget-aware menu generation';
