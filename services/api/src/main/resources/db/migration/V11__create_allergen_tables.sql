-- V11__create_allergen_tables.sql
-- Create allergen and dietary restriction tables

-- Create allergen master table
CREATE TABLE "allergen" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "name" TEXT NOT NULL UNIQUE,
    "description" TEXT,
    "severity_level" TEXT CHECK ("severity_level" IN ('LOW', 'MODERATE', 'HIGH', 'SEVERE')),
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create patient allergen associations
CREATE TABLE "patient_allergen" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "allergen_id" UUID NOT NULL REFERENCES "allergen"("id") ON DELETE CASCADE,
    "severity" TEXT NOT NULL CHECK ("severity" IN ('MILD', 'MODERATE', 'SEVERE', 'ANAPHYLACTIC')),
    "notes" TEXT,
    "diagnosed_date" DATE,
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_patient_allergen" UNIQUE ("patient_id", "allergen_id")
);

-- Create patient forbidden ingredients table
CREATE TABLE "patient_forbidden_ingredient" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "patient_id" UUID NOT NULL REFERENCES "patient_profile"("id") ON DELETE CASCADE,
    "ingredient_name" TEXT NOT NULL,
    "reason" TEXT CHECK ("reason" IN ('ALLERGY', 'INTOLERANCE', 'PREFERENCE', 'MEDICAL', 'RELIGIOUS')),
    "severity" TEXT CHECK ("severity" IN ('AVOID', 'LIMIT', 'MONITOR')),
    "notes" TEXT,
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_patient_ingredient" UNIQUE ("patient_id", "ingredient_name")
);

-- Create product allergen associations
CREATE TABLE "product_allergen" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "product_id" UUID NOT NULL REFERENCES "products"("id") ON DELETE CASCADE,
    "allergen_id" UUID NOT NULL REFERENCES "allergen"("id") ON DELETE CASCADE,
    "contains_level" TEXT NOT NULL CHECK ("contains_level" IN ('CONTAINS', 'MAY_CONTAIN', 'TRACES')),
    "verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_product_allergen" UNIQUE ("product_id", "allergen_id")
);

-- Create custom product allergen associations
CREATE TABLE "custom_product_allergen" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "custom_product_id" UUID NOT NULL REFERENCES "custom_product"("id") ON DELETE CASCADE,
    "allergen_id" UUID NOT NULL REFERENCES "allergen"("id") ON DELETE CASCADE,
    "contains_level" TEXT NOT NULL CHECK ("contains_level" IN ('CONTAINS', 'MAY_CONTAIN', 'TRACES')),
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_custom_product_allergen" UNIQUE ("custom_product_id", "allergen_id")
);

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_allergen_updated_at
    BEFORE UPDATE ON "allergen"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_patient_allergen_updated_at
    BEFORE UPDATE ON "patient_allergen"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_patient_forbidden_ingredient_updated_at
    BEFORE UPDATE ON "patient_forbidden_ingredient"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_product_allergen_updated_at
    BEFORE UPDATE ON "product_allergen"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_custom_product_allergen_updated_at
    BEFORE UPDATE ON "custom_product_allergen"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_allergen_name ON "allergen"("name");
CREATE INDEX IF NOT EXISTS idx_allergen_active ON "allergen"("is_active");

CREATE INDEX IF NOT EXISTS idx_patient_allergen_patient_id ON "patient_allergen"("patient_id");
CREATE INDEX IF NOT EXISTS idx_patient_allergen_allergen_id ON "patient_allergen"("allergen_id");
CREATE INDEX IF NOT EXISTS idx_patient_allergen_active ON "patient_allergen"("is_active");

CREATE INDEX IF NOT EXISTS idx_patient_forbidden_patient_id ON "patient_forbidden_ingredient"("patient_id");
CREATE INDEX IF NOT EXISTS idx_patient_forbidden_ingredient ON "patient_forbidden_ingredient"("ingredient_name");
CREATE INDEX IF NOT EXISTS idx_patient_forbidden_active ON "patient_forbidden_ingredient"("is_active");

CREATE INDEX IF NOT EXISTS idx_product_allergen_product_id ON "product_allergen"("product_id");
CREATE INDEX IF NOT EXISTS idx_product_allergen_allergen_id ON "product_allergen"("allergen_id");

CREATE INDEX IF NOT EXISTS idx_custom_product_allergen_product_id ON "custom_product_allergen"("custom_product_id");
CREATE INDEX IF NOT EXISTS idx_custom_product_allergen_allergen_id ON "custom_product_allergen"("allergen_id");

-- Insert common allergens
INSERT INTO "allergen" ("name", "description", "severity_level") VALUES
('Gluten', 'Proteins found in wheat, barley, rye, and triticale', 'HIGH'),
('Dairy/Milk', 'Milk proteins including casein and whey', 'MODERATE'),
('Eggs', 'Chicken egg proteins', 'MODERATE'),
('Nuts', 'Tree nuts including almonds, walnuts, cashews', 'SEVERE'),
('Peanuts', 'Legume allergen, not a true nut', 'SEVERE'),
('Soy', 'Soybean proteins', 'MODERATE'),
('Fish', 'Finned fish proteins', 'HIGH'),
('Shellfish', 'Crustaceans and mollusks', 'HIGH'),
('Sesame', 'Sesame seed proteins', 'MODERATE'),
('Sulfites', 'Sulfur dioxide and sulfites', 'LOW'),
('Mustard', 'Mustard seed proteins', 'LOW'),
('Celery', 'Celery proteins', 'LOW'),
('Lupin', 'Lupin bean proteins', 'MODERATE');

