-- Create product_translations table for multi-language support
-- This table stores localized product names and categories

CREATE TABLE IF NOT EXISTS product_translations (
  id BIGSERIAL PRIMARY KEY,
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  locale VARCHAR(8) NOT NULL,           -- 'ka', 'ru', 'en'
  product_name TEXT NOT NULL,
  category TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create unique constraint to prevent duplicate translations for same product+locale
CREATE UNIQUE INDEX IF NOT EXISTS ux_product_translations_product_locale
  ON product_translations (product_id, locale);

-- Create index for locale-based queries
CREATE INDEX IF NOT EXISTS ix_product_translations_locale
  ON product_translations (locale);

-- Create index for product_name searches (case-insensitive)
CREATE INDEX IF NOT EXISTS ix_product_translations_name_lower
  ON product_translations (LOWER(product_name));

-- Optional: Create trigram extension for fuzzy text search (if available)
-- This will be created in a separate migration if the extension is available
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX IF NOT EXISTS ix_trgm_translations_name
--   ON product_translations USING GIN (product_name gin_trgm_ops);

-- Add comments for documentation
COMMENT ON TABLE product_translations IS 'Stores localized product names and categories for multi-language support';
COMMENT ON COLUMN product_translations.locale IS 'Language code: ka (Georgian), ru (Russian), en (English)';
COMMENT ON COLUMN product_translations.product_name IS 'Localized product name';
COMMENT ON COLUMN product_translations.category IS 'Localized category name';
