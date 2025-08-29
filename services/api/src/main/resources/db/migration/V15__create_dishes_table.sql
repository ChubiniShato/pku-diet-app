-- V15__create_dishes_table.sql
-- Create dishes table for pre-defined meal combinations

CREATE TABLE "dishes" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "name" TEXT NOT NULL,
    "category" TEXT,
    "description" TEXT,
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
    "preparation_time_minutes" INTEGER,
    "difficulty_level" TEXT CHECK ("difficulty_level" IN ('EASY', 'MEDIUM', 'HARD')),
    "recipe_instructions" TEXT,
    "is_verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "is_visible" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create dish_ingredient table to track what products make up each dish
CREATE TABLE "dish_ingredient" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "dish_id" UUID NOT NULL REFERENCES "dishes"("id") ON DELETE CASCADE,
    "product_id" UUID NOT NULL REFERENCES "products"("id") ON DELETE CASCADE,
    "quantity_grams" NUMERIC(8,2) NOT NULL,
    "preparation_notes" TEXT,
    "is_optional" BOOLEAN NOT NULL DEFAULT FALSE,
    "sort_order" INTEGER NOT NULL DEFAULT 0,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "positive_quantity" CHECK ("quantity_grams" > 0),
    CONSTRAINT "unique_dish_product" UNIQUE ("dish_id", "product_id")
);

-- Create dish_allergen associations
CREATE TABLE "dish_allergen" (
    "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    "dish_id" UUID NOT NULL REFERENCES "dishes"("id") ON DELETE CASCADE,
    "allergen_id" UUID NOT NULL REFERENCES "allergen"("id") ON DELETE CASCADE,
    "contains_level" TEXT NOT NULL CHECK ("contains_level" IN ('CONTAINS', 'MAY_CONTAIN', 'TRACES')),
    "verified" BOOLEAN NOT NULL DEFAULT FALSE,
    "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "unique_dish_allergen" UNIQUE ("dish_id", "allergen_id")
);

-- Add foreign key constraint to menu_entry for dish_id (now that dishes table exists)
ALTER TABLE "menu_entry" 
ADD CONSTRAINT "fk_menu_entry_dish_id" 
FOREIGN KEY ("dish_id") REFERENCES "dishes"("id") ON DELETE CASCADE;

-- Create triggers for updated_at timestamps
CREATE TRIGGER update_dishes_updated_at
    BEFORE UPDATE ON "dishes"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dish_allergen_updated_at
    BEFORE UPDATE ON "dish_allergen"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_dishes_name ON "dishes"("name");
CREATE INDEX IF NOT EXISTS idx_dishes_category ON "dishes"("category");
CREATE INDEX IF NOT EXISTS idx_dishes_verified ON "dishes"("is_verified");
CREATE INDEX IF NOT EXISTS idx_dishes_visible ON "dishes"("is_visible");
CREATE INDEX IF NOT EXISTS idx_dishes_phenylalanine ON "dishes"("per100_phenylalanine");

CREATE INDEX IF NOT EXISTS idx_dish_ingredient_dish_id ON "dish_ingredient"("dish_id");
CREATE INDEX IF NOT EXISTS idx_dish_ingredient_product_id ON "dish_ingredient"("product_id");
CREATE INDEX IF NOT EXISTS idx_dish_ingredient_sort_order ON "dish_ingredient"("sort_order");

CREATE INDEX IF NOT EXISTS idx_dish_allergen_dish_id ON "dish_allergen"("dish_id");
CREATE INDEX IF NOT EXISTS idx_dish_allergen_allergen_id ON "dish_allergen"("allergen_id");

-- Insert sample dishes (migrating from existing data if any)
INSERT INTO "dishes" (
    "id", "name", "category", "nominal_serving_grams", "manual_serving_override",
    "total_phenylalanine", "total_leucine", "total_tyrosine", "total_methionine",
    "total_kilojoules", "total_kilocalories", "total_protein", "total_carbohydrates", "total_fats",
    "per100_phenylalanine", "per100_leucine", "per100_tyrosine", "per100_methionine",
    "per100_kilojoules", "per100_kilocalories", "per100_protein", "per100_carbohydrates", "per100_fats",
    "difficulty_level", "is_verified"
) VALUES (
    'a1b2c3d4-e5f6-7890-abcd-123456789abc',
    'PKU-Safe Breakfast Bowl',
    'Breakfast',
    250.00,
    false,
    45.50, 85.20, 32.10, 28.75, 
    1250.00, 298.50, 3.25, 58.40, 8.90,
    18.20, 34.08, 12.84, 11.50,
    500.00, 119.40, 1.30, 23.36, 3.56,
    'EASY',
    true
) ON CONFLICT (id) DO NOTHING;

INSERT INTO "dishes" (
    "id", "name", "category", "nominal_serving_grams", "manual_serving_override",
    "total_phenylalanine", "total_leucine", "total_tyrosine", "total_methionine",
    "total_kilojoules", "total_kilocalories", "total_protein", "total_carbohydrates", "total_fats",
    "per100_phenylalanine", "per100_leucine", "per100_tyrosine", "per100_methionine",
    "per100_kilojoules", "per100_kilocalories", "per100_protein", "per100_carbohydrates", "per100_fats",
    "difficulty_level", "is_verified"
) VALUES (
    'b2c3d4e5-f6a7-4901-bcde-234567890def',
    'PKU Lunch Salad',
    'Lunch',
    180.00,
    false,
    25.20, 48.60, 18.90, 15.30,
    720.00, 172.00, 1.80, 32.40, 4.50,
    14.00, 27.00, 10.50, 8.50,
    400.00, 95.56, 1.00, 18.00, 2.50,
    'EASY',
    true
) ON CONFLICT (id) DO NOTHING;


