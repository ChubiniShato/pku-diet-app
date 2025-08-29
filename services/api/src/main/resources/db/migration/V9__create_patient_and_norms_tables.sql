-- V9__create_patient_and_norms_tables.sql
-- Create patient profile and norms prescription tables

-- Create patient_profile table
CREATE TABLE "patient_profile" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "name" TEXT NOT NULL,
    "birth_date" DATE,
    "weight_kg" NUMERIC(5,2),
    "height_cm" NUMERIC(5,2),
    "activity_level" TEXT CHECK ("activity_level" IN ('LOW', 'MODERATE', 'HIGH')),
    "region" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create norm_prescription table
CREATE TABLE "norm_prescription" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "phe_limit_mg_per_day" NUMERIC(8,2) NOT NULL,
    "protein_limit_g_per_day" NUMERIC(8,2),
    "kcal_min_per_day" NUMERIC(8,2),
    "fat_limit_g_per_day" NUMERIC(8,2),
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "prescribed_date" DATE NOT NULL DEFAULT CURRENT_DATE,
    "notes" TEXT,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_active_prescription_per_patient" 
    EXCLUDE ("patient_id" WITH =) WHERE ("is_active" = TRUE)
);

-- Create custom_product table for user-defined products
CREATE TABLE "custom_product" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "name" TEXT NOT NULL,
    "category" TEXT,
    "standard_serving_grams" NUMERIC(8,2),
    "manual_serving_override" BOOLEAN NOT NULL DEFAULT FALSE,
    "phenylalanine" NUMERIC(8,2),
    "leucine" NUMERIC(8,2),
    "tyrosine" NUMERIC(8,2),
    "methionine" NUMERIC(8,2),
    "kilojoules" NUMERIC(8,2),
    "kilocalories" NUMERIC(8,2),
    "protein" NUMERIC(8,2),
    "carbohydrates" NUMERIC(8,2),
    "fats" NUMERIC(8,2),
    "is_visible" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create custom_dish table for user-defined dishes
CREATE TABLE "custom_dish" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "name" TEXT NOT NULL,
    "category" TEXT,
    "nominal_serving_grams" NUMERIC(8,2) NOT NULL,
    "manual_serving_override" BOOLEAN NOT NULL DEFAULT FALSE,
    "total_phenylalanine" NUMERIC(8,2),
    "total_leucine" NUMERIC(8,2),
    "total_tyrosine" NUMERIC(8,2),
    "total_methionine" NUMERIC(8,2),
    "total_kilojoules" NUMERIC(8,2),
    "total_kilocalories" NUMERIC(8,2),
    "total_protein" NUMERIC(8,2),
    "total_carbohydrates" NUMERIC(8,2),
    "total_fats" NUMERIC(8,2),
    "per100_phenylalanine" NUMERIC(8,2),
    "per100_leucine" NUMERIC(8,2),
    "per100_tyrosine" NUMERIC(8,2),
    "per100_methionine" NUMERIC(8,2),
    "per100_kilojoules" NUMERIC(8,2),
    "per100_kilocalories" NUMERIC(8,2),
    "per100_protein" NUMERIC(8,2),
    "per100_carbohydrates" NUMERIC(8,2),
    "per100_fats" NUMERIC(8,2),
    "is_visible" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_patient_profile_updated_at
    BEFORE UPDATE ON "patient_profile"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_norm_prescription_updated_at
    BEFORE UPDATE ON "norm_prescription"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_custom_product_updated_at
    BEFORE UPDATE ON "custom_product"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_custom_dish_updated_at
    BEFORE UPDATE ON "custom_dish"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_norm_prescription_patient_id ON "norm_prescription"("patient_id");
CREATE INDEX IF NOT EXISTS idx_norm_prescription_is_active ON "norm_prescription"("is_active");
CREATE INDEX IF NOT EXISTS idx_custom_product_patient_id ON "custom_product"("patient_id");
CREATE INDEX IF NOT EXISTS idx_custom_dish_patient_id ON "custom_dish"("patient_id");
CREATE INDEX IF NOT EXISTS idx_patient_profile_name ON "patient_profile"("name");

