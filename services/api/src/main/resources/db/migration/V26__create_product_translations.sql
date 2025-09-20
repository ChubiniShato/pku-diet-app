-- V26__create_product_translations.sql
-- Create product_translations table for multi-language support

CREATE TABLE IF NOT EXISTS product_translations (
  id BIGSERIAL PRIMARY KEY,
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  locale VARCHAR(8) NOT NULL,           -- 'ka', 'ru', 'en'
  product_name TEXT NOT NULL,
  category TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE (product_id, locale)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS ix_translations_locale ON product_translations(locale);
CREATE INDEX IF NOT EXISTS ix_translations_product_id ON product_translations(product_id);
CREATE INDEX IF NOT EXISTS ix_translations_name_lower ON product_translations(LOWER(product_name));

-- Add comments for documentation
COMMENT ON TABLE product_translations IS 'Stores localized product names and categories for multi-language support';
COMMENT ON COLUMN product_translations.locale IS 'Language code: ka (Georgian), ru (Russian), en (English)';
COMMENT ON COLUMN product_translations.product_name IS 'Localized product name';
COMMENT ON COLUMN product_translations.category IS 'Localized category name';
