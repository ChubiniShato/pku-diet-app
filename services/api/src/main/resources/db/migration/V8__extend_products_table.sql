-- V8__extend_products_table.sql
-- Extend products table with serving fields and verification/visibility flags

-- First, change the primary key from SERIAL to UUID
ALTER TABLE "products" DROP CONSTRAINT products_pkey;
ALTER TABLE "products" DROP COLUMN "id";
ALTER TABLE "products" ADD COLUMN "id" UUID PRIMARY KEY DEFAULT uuid_generate_v4();

-- Add serving and verification columns to products table
ALTER TABLE "products" 
ADD COLUMN "standard_serving_grams" NUMERIC(8,2),
ADD COLUMN "manual_serving_override" BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN "is_verified" BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN "is_visible" BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN "created_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN "updated_at" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON "products"
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_products_is_verified ON "products"("is_verified");
CREATE INDEX IF NOT EXISTS idx_products_is_visible ON "products"("is_visible");
CREATE INDEX IF NOT EXISTS idx_products_created_at ON "products"("created_at");

-- Update existing products with default serving sizes based on category
UPDATE "products" 
SET "standard_serving_grams" = CASE 
    WHEN "category" = 'Vegetables and Mushrooms' THEN 100.0
    WHEN "category" = 'Potato Products' THEN 150.0
    ELSE 100.0
END
WHERE "standard_serving_grams" IS NULL;

-- Mark existing products as verified
UPDATE "products" SET "is_verified" = TRUE WHERE "is_verified" = FALSE;
