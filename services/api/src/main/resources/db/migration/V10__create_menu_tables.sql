-- V10__create_menu_tables.sql
-- Create menu system tables for weekly and daily menus

-- Create menu_week table
CREATE TABLE "menu_week" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "week_start_date" DATE NOT NULL,
    "week_end_date" DATE NOT NULL,
    "status" TEXT NOT NULL CHECK ("status" IN ('DRAFT', 'GENERATED', 'APPROVED', 'CONSUMED', 'ARCHIVED')),
    "generation_method" TEXT CHECK ("generation_method" IN ('MANUAL', 'HEURISTIC', 'OPTIMIZED')),
    "total_week_phe_mg" NUMERIC(10,2),
    "total_week_protein_g" NUMERIC(10,2),
    "total_week_kcal" NUMERIC(10,2),
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "valid_week_dates" CHECK ("week_end_date" >= "week_start_date")
);

-- Create menu_day table
CREATE TABLE "menu_day" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "menu_week_id" UUID REFERENCES "menu_week"("id") ON DELETE CASCADE,
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "date" DATE NOT NULL,
    "day_of_week" INTEGER NOT NULL CHECK ("day_of_week" BETWEEN 1 AND 7),
    "status" TEXT NOT NULL CHECK ("status" IN ('DRAFT', 'GENERATED', 'APPROVED', 'CONSUMED', 'ARCHIVED')),
    "total_day_phe_mg" NUMERIC(8,2),
    "total_day_protein_g" NUMERIC(8,2),
    "total_day_kcal" NUMERIC(8,2),
    "total_day_fat_g" NUMERIC(8,2),
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_patient_date" UNIQUE ("patient_id", "date")
);

-- Create meal_slot table for different meal times
CREATE TABLE "meal_slot" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "menu_day_id" UUID NOT NULL REFERENCES "menu_day"("id") ON DELETE CASCADE,
    "slot_name" TEXT NOT NULL CHECK ("slot_name" IN ('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'EVENING_SNACK')),
    "slot_order" INTEGER NOT NULL,
    "target_phe_mg" NUMERIC(8,2),
    "target_kcal" NUMERIC(8,2),
    "actual_phe_mg" NUMERIC(8,2),
    "actual_protein_g" NUMERIC(8,2),
    "actual_kcal" NUMERIC(8,2),
    "actual_fat_g" NUMERIC(8,2),
    "is_consumed" BOOLEAN NOT NULL DEFAULT FALSE,
    "consumed_at" TIMESTAMP WITH TIME ZONE,
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_day_slot" UNIQUE ("menu_day_id", "slot_name")
);

-- Create menu_entry table for individual food items in meal slots
CREATE TABLE "menu_entry" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "meal_slot_id" UUID NOT NULL REFERENCES "meal_slot"("id") ON DELETE CASCADE,
    "entry_type" TEXT NOT NULL CHECK ("entry_type" IN ('PRODUCT', 'CUSTOM_PRODUCT', 'DISH', 'CUSTOM_DISH')),
    "product_id" UUID REFERENCES "products"("id"),
    "custom_product_id" UUID REFERENCES "custom_product"("id"),
    "dish_id" UUID, -- Will reference dishes table when created
    "custom_dish_id" UUID REFERENCES "custom_dish"("id"),
    "planned_serving_grams" NUMERIC(8,2) NOT NULL,
    "actual_serving_grams" NUMERIC(8,2),
    "calculated_phe_mg" NUMERIC(8,2),
    "calculated_protein_g" NUMERIC(8,2),
    "calculated_kcal" NUMERIC(8,2),
    "calculated_fat_g" NUMERIC(8,2),
    "is_alternative" BOOLEAN NOT NULL DEFAULT FALSE,
    "alternative_group" INTEGER,
    "is_consumed" BOOLEAN NOT NULL DEFAULT FALSE,
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "valid_entry_reference" CHECK (
        (entry_type = 'PRODUCT' AND product_id IS NOT NULL AND custom_product_id IS NULL AND dish_id IS NULL AND custom_dish_id IS NULL) OR
        (entry_type = 'CUSTOM_PRODUCT' AND custom_product_id IS NOT NULL AND product_id IS NULL AND dish_id IS NULL AND custom_dish_id IS NULL) OR
        (entry_type = 'DISH' AND dish_id IS NOT NULL AND product_id IS NULL AND custom_product_id IS NULL AND custom_dish_id IS NULL) OR
        (entry_type = 'CUSTOM_DISH' AND custom_dish_id IS NOT NULL AND product_id IS NULL AND custom_product_id IS NULL AND dish_id IS NULL)
    )
);

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_menu_week_updated_at
    BEFORE UPDATE ON "menu_week"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_day_updated_at
    BEFORE UPDATE ON "menu_day"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meal_slot_updated_at
    BEFORE UPDATE ON "meal_slot"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_entry_updated_at
    BEFORE UPDATE ON "menu_entry"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_menu_week_patient_id ON "menu_week"("patient_id");
CREATE INDEX IF NOT EXISTS idx_menu_week_start_date ON "menu_week"("week_start_date");
CREATE INDEX IF NOT EXISTS idx_menu_week_status ON "menu_week"("status");

CREATE INDEX IF NOT EXISTS idx_menu_day_patient_id ON "menu_day"("patient_id");
CREATE INDEX IF NOT EXISTS idx_menu_day_date ON "menu_day"("date");
CREATE INDEX IF NOT EXISTS idx_menu_day_status ON "menu_day"("status");
CREATE INDEX IF NOT EXISTS idx_menu_day_week_id ON "menu_day"("menu_week_id");

CREATE INDEX IF NOT EXISTS idx_meal_slot_menu_day_id ON "meal_slot"("menu_day_id");
CREATE INDEX IF NOT EXISTS idx_meal_slot_name ON "meal_slot"("slot_name");
CREATE INDEX IF NOT EXISTS idx_meal_slot_consumed ON "meal_slot"("is_consumed");

CREATE INDEX IF NOT EXISTS idx_menu_entry_meal_slot_id ON "menu_entry"("meal_slot_id");
CREATE INDEX IF NOT EXISTS idx_menu_entry_type ON "menu_entry"("entry_type");
CREATE INDEX IF NOT EXISTS idx_menu_entry_product_id ON "menu_entry"("product_id");
CREATE INDEX IF NOT EXISTS idx_menu_entry_consumed ON "menu_entry"("is_consumed");
CREATE INDEX IF NOT EXISTS idx_menu_entry_alternative ON "menu_entry"("is_alternative", "alternative_group");

