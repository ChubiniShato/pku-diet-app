-- V29: Add performance indexes for better query performance
-- These indexes will improve performance for common query patterns

-- Products table indexes
CREATE INDEX IF NOT EXISTS idx_products_phe ON products(phenylalanine);
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_code ON products(product_code);

-- Product translations indexes
CREATE INDEX IF NOT EXISTS idx_pt_product_locale ON product_translations(product_id, locale);
CREATE INDEX IF NOT EXISTS idx_pt_locale_name ON product_translations(locale, product_name);

-- Additional performance indexes for common queries
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(product_name);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at);

-- Menu-related indexes (only if tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'menu_days') THEN
        CREATE INDEX IF NOT EXISTS idx_menu_days_patient_date ON menu_days(patient_id, date);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'menu_entries') THEN
        CREATE INDEX IF NOT EXISTS idx_menu_entries_slot ON menu_entries(meal_slot_id);
    END IF;
END $$;

-- Pantry indexes (only if tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pantry_item') THEN
        CREATE INDEX IF NOT EXISTS idx_pantry_patient_available ON pantry_item(patient_id, is_available);
        CREATE INDEX IF NOT EXISTS idx_pantry_expiry ON pantry_item(expiry_date);
    END IF;
END $$;

-- Price indexes (only if tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'price_entry') THEN
        CREATE INDEX IF NOT EXISTS idx_price_product_date ON price_entry(product_id, created_at);
        CREATE INDEX IF NOT EXISTS idx_price_current ON price_entry(is_current);
    END IF;
END $$;
