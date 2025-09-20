-- V25__add_product_code_to_products.sql
-- Add product_code field to products table for stable product identification

-- Add product_code column (nullable initially to allow existing data)
ALTER TABLE products ADD COLUMN IF NOT EXISTS product_code TEXT;

-- Backfill from product_number when present (only if product_code doesn't exist)
UPDATE products
SET product_code = 'P' || lpad(product_number::text, 6, '0')
WHERE product_code IS NULL AND product_number IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM products p2 
    WHERE p2.product_code = 'P' || lpad(products.product_number::text, 6, '0')
  );

-- Fallback generator for remaining nulls (order by id for stability)
WITH numbered AS (
  SELECT id, row_number() OVER (ORDER BY id) AS rn
  FROM products
  WHERE product_code IS NULL
)
UPDATE products p
SET product_code = 'P' || to_char(clock_timestamp(),'YYMMDDHH24MISS') || lpad(n.rn::text, 4, '0')
FROM numbered n
WHERE p.id = n.id;

-- Enforce constraints after fill
ALTER TABLE products ALTER COLUMN product_code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_products_code ON products(product_code);

-- Add comment for documentation
COMMENT ON COLUMN products.product_code IS 'Unique product identifier used for matching with translations during CSV uploads';
