-- V12__create_pantry_tables.sql
-- Create pantry and pricing tables

-- Create pantry_item table for tracking available ingredients
CREATE TABLE "pantry_item" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "product_id" UUID REFERENCES "products"("id") ON DELETE CASCADE,
    "custom_product_id" UUID REFERENCES "custom_product"("id") ON DELETE CASCADE,
    "item_type" TEXT NOT NULL CHECK ("item_type" IN ('PRODUCT', 'CUSTOM_PRODUCT')),
    "quantity_grams" NUMERIC(10,2) NOT NULL,
    "purchase_date" DATE,
    "expiry_date" DATE,
    "location" TEXT, -- e.g., 'FRIDGE', 'PANTRY', 'FREEZER'
    "cost_per_unit" NUMERIC(10,2),
    "currency" TEXT DEFAULT 'USD',
    "notes" TEXT,
    "is_available" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "valid_pantry_reference" CHECK (
        (item_type = 'PRODUCT' AND product_id IS NOT NULL AND custom_product_id IS NULL) OR
        (item_type = 'CUSTOM_PRODUCT' AND custom_product_id IS NOT NULL AND product_id IS NULL)
    ),
    CONSTRAINT "positive_quantity" CHECK ("quantity_grams" > 0)
);

-- Create price_entry table for tracking product costs
CREATE TABLE "price_entry" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "product_id" UUID REFERENCES "products"("id") ON DELETE CASCADE,
    "custom_product_id" UUID REFERENCES "custom_product"("id") ON DELETE CASCADE,
    "item_type" TEXT NOT NULL CHECK ("item_type" IN ('PRODUCT', 'CUSTOM_PRODUCT')),
    "store_name" TEXT,
    "region" TEXT,
    "price_per_unit" NUMERIC(10,2) NOT NULL,
    "unit_size_grams" NUMERIC(10,2) NOT NULL,
    "currency" TEXT NOT NULL DEFAULT 'USD',
    "recorded_date" DATE NOT NULL DEFAULT CURRENT_DATE,
    "is_current" BOOLEAN NOT NULL DEFAULT TRUE,
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "valid_price_reference" CHECK (
        (item_type = 'PRODUCT' AND product_id IS NOT NULL AND custom_product_id IS NULL) OR
        (item_type = 'CUSTOM_PRODUCT' AND custom_product_id IS NOT NULL AND product_id IS NULL)
    ),
    CONSTRAINT "positive_price" CHECK ("price_per_unit" > 0),
    CONSTRAINT "positive_unit_size" CHECK ("unit_size_grams" > 0)
);

-- Create budget tracking table
CREATE TABLE "budget_tracking" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "month_year" DATE NOT NULL, -- First day of the month
    "budget_limit" NUMERIC(10,2),
    "spent_amount" NUMERIC(10,2) NOT NULL DEFAULT 0,
    "currency" TEXT NOT NULL DEFAULT 'USD',
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_patient_month" UNIQUE ("patient_id", "month_year"),
    CONSTRAINT "positive_budget" CHECK ("budget_limit" IS NULL OR "budget_limit" > 0),
    CONSTRAINT "non_negative_spent" CHECK ("spent_amount" >= 0)
);

-- Create pantry_transaction table for tracking pantry changes
CREATE TABLE "pantry_transaction" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "pantry_item_id" UUID NOT NULL REFERENCES "pantry_item"("id") ON DELETE CASCADE,
    "transaction_type" TEXT NOT NULL CHECK ("transaction_type" IN ('ADD', 'CONSUME', 'EXPIRE', 'ADJUST')),
    "quantity_change_grams" NUMERIC(10,2) NOT NULL,
    "transaction_date" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "menu_entry_id" UUID REFERENCES "menu_entry"("id"), -- If consumed via menu
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_pantry_item_updated_at
    BEFORE UPDATE ON "pantry_item"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_price_entry_updated_at
    BEFORE UPDATE ON "price_entry"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_budget_tracking_updated_at
    BEFORE UPDATE ON "budget_tracking"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_pantry_item_patient_id ON "pantry_item"("patient_id");
CREATE INDEX IF NOT EXISTS idx_pantry_item_product_id ON "pantry_item"("product_id");
CREATE INDEX IF NOT EXISTS idx_pantry_item_custom_product_id ON "pantry_item"("custom_product_id");
CREATE INDEX IF NOT EXISTS idx_pantry_item_expiry_date ON "pantry_item"("expiry_date");
CREATE INDEX IF NOT EXISTS idx_pantry_item_available ON "pantry_item"("is_available");

CREATE INDEX IF NOT EXISTS idx_price_entry_product_id ON "price_entry"("product_id");
CREATE INDEX IF NOT EXISTS idx_price_entry_custom_product_id ON "price_entry"("custom_product_id");
CREATE INDEX IF NOT EXISTS idx_price_entry_current ON "price_entry"("is_current");
CREATE INDEX IF NOT EXISTS idx_price_entry_date ON "price_entry"("recorded_date");
CREATE INDEX IF NOT EXISTS idx_price_entry_region ON "price_entry"("region");

CREATE INDEX IF NOT EXISTS idx_budget_tracking_patient_id ON "budget_tracking"("patient_id");
CREATE INDEX IF NOT EXISTS idx_budget_tracking_month ON "budget_tracking"("month_year");

CREATE INDEX IF NOT EXISTS idx_pantry_transaction_item_id ON "pantry_transaction"("pantry_item_id");
CREATE INDEX IF NOT EXISTS idx_pantry_transaction_type ON "pantry_transaction"("transaction_type");
CREATE INDEX IF NOT EXISTS idx_pantry_transaction_date ON "pantry_transaction"("transaction_date");
CREATE INDEX IF NOT EXISTS idx_pantry_transaction_menu_entry ON "pantry_transaction"("menu_entry_id");

