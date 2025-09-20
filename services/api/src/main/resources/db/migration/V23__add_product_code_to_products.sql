-- Add product_code field to products table for stable product identification
-- This field will be used to match products with translations during CSV uploads

-- Add product_code column (nullable initially to allow existing data)
ALTER TABLE products ADD COLUMN IF NOT EXISTS product_code VARCHAR(50);

-- Create unique index on product_code for fast lookups
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_product_code 
  ON products (product_code) 
  WHERE product_code IS NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN products.product_code IS 'Unique product identifier used for matching with translations during CSV uploads';

-- Note: Existing products will need to have product_code populated manually
-- or through a data migration script. This is intentionally left as nullable
-- to avoid breaking existing data.
