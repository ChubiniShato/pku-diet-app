-- V4__create_dishes_tables.sql
-- Create dishes and dish_items tables for PKU diet planning

-- Create dishes table
CREATE TABLE IF NOT EXISTS "dishes" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" TEXT NOT NULL,
    "category" TEXT,
    "nominal_serving_grams" NUMERIC(10,2) NOT NULL,
    "manual_serving_override" BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Total nutritional values (based on nominal serving)
    "total_phenylalanine" NUMERIC(10,2),
    "total_leucine" NUMERIC(10,2),
    "total_tyrosine" NUMERIC(10,2),
    "total_methionine" NUMERIC(10,2),
    "total_kilojoules" NUMERIC(10,2),
    "total_kilocalories" NUMERIC(10,2),
    "total_protein" NUMERIC(10,2),
    "total_carbohydrates" NUMERIC(10,2),
    "total_fats" NUMERIC(10,2),
    
    -- Per 100g nutritional values (normalized)
    "per100_phenylalanine" NUMERIC(10,2),
    "per100_leucine" NUMERIC(10,2),
    "per100_tyrosine" NUMERIC(10,2),
    "per100_methionine" NUMERIC(10,2),
    "per100_kilojoules" NUMERIC(10,2),
    "per100_kilocalories" NUMERIC(10,2),
    "per100_protein" NUMERIC(10,2),
    "per100_carbohydrates" NUMERIC(10,2),
    "per100_fats" NUMERIC(10,2),
    
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create dish_items table
CREATE TABLE IF NOT EXISTS "dish_items" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "dish_id" UUID NOT NULL,
    "product_id" UUID NOT NULL,
    "grams" NUMERIC(10,2) NOT NULL,
    
    -- Snapshot of product nutritional values per 100g at creation time
    "snapshot_phenylalanine" NUMERIC(10,2),
    "snapshot_leucine" NUMERIC(10,2),
    "snapshot_tyrosine" NUMERIC(10,2),
    "snapshot_methionine" NUMERIC(10,2),
    "snapshot_kilojoules" INTEGER,
    "snapshot_kilocalories" INTEGER,
    "snapshot_protein" NUMERIC(10,2),
    "snapshot_carbohydrates" NUMERIC(10,2),
    "snapshot_fats" NUMERIC(10,2),
    
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "fk_dish_items_dish" FOREIGN KEY ("dish_id") REFERENCES "dishes"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_dish_items_product" FOREIGN KEY ("product_id") REFERENCES "products"("id") ON DELETE RESTRICT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS "idx_dishes_name" ON "dishes"("name");
CREATE INDEX IF NOT EXISTS "idx_dishes_category" ON "dishes"("category");
CREATE INDEX IF NOT EXISTS "idx_dishes_per100_phenylalanine" ON "dishes"("per100_phenylalanine");
CREATE INDEX IF NOT EXISTS "idx_dishes_per100_protein" ON "dishes"("per100_protein");
CREATE INDEX IF NOT EXISTS "idx_dishes_created_at" ON "dishes"("created_at");

CREATE INDEX IF NOT EXISTS "idx_dish_items_dish_id" ON "dish_items"("dish_id");
CREATE INDEX IF NOT EXISTS "idx_dish_items_product_id" ON "dish_items"("product_id");
CREATE INDEX IF NOT EXISTS "idx_dish_items_grams" ON "dish_items"("grams");

-- Add constraints
ALTER TABLE "dishes" ADD CONSTRAINT "chk_dishes_nominal_serving_positive" CHECK ("nominal_serving_grams" > 0);
ALTER TABLE "dish_items" ADD CONSTRAINT "chk_dish_items_grams_positive" CHECK ("grams" > 0);

-- Add comments for documentation
COMMENT ON TABLE "dishes" IS 'PKU diet dishes - meals composed of multiple products';
COMMENT ON COLUMN "dishes"."nominal_serving_grams" IS 'Total grams for the dish - either auto-calculated sum or manually set';
COMMENT ON COLUMN "dishes"."manual_serving_override" IS 'True if nominal_serving_grams is manually set and should not auto-update';

COMMENT ON TABLE "dish_items" IS 'Individual product items within a dish';
COMMENT ON COLUMN "dish_items"."snapshot_phenylalanine" IS 'Historical snapshot of product PHE per 100g at time of adding to dish';
COMMENT ON COLUMN "dish_items"."snapshot_protein" IS 'Historical snapshot of product protein per 100g at time of adding to dish';
