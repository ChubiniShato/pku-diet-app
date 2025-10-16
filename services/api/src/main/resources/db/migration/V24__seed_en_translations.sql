-- Backfill English translations from existing product data
-- This migration copies existing product_name and category from products table
-- into product_translations table as English translations

-- Insert English translations for all existing products
-- Only insert if no translation already exists for the product
INSERT INTO product_translations (product_id, locale, product_name, category)
SELECT 
    p.id, 
    'en', 
    p.product_name, 
    p.category
FROM products p
WHERE p.product_name IS NOT NULL 
  AND p.product_name != ''
  AND NOT EXISTS (
    SELECT 1 FROM product_translations pt 
    WHERE pt.product_id = p.id AND pt.locale = 'en'
  );

-- Add comment for documentation
COMMENT ON TABLE product_translations IS 'English translations seeded from existing product data';
