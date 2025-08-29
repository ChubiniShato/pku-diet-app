-- V2__create_products_table.sql
-- Create the products table for PKU diet planning

CREATE TABLE IF NOT EXISTS "products" (
    "id" SERIAL PRIMARY KEY,
    "category" TEXT NOT NULL,
    "product_number" INTEGER,
    "product_name" TEXT NOT NULL,
    "phenylalanine" NUMERIC(8,2),
    "leucine" NUMERIC(8,2),
    "tyrosine" NUMERIC(8,2),
    "methionine" NUMERIC(8,2),
    "kilojoules" NUMERIC(8,2),
    "kilocalories" NUMERIC(8,2),
    "protein" NUMERIC(8,2),
    "carbohydrates" NUMERIC(8,2),
    "fats" NUMERIC(8,2)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_category ON products("category");
CREATE INDEX IF NOT EXISTS idx_products_phenylalanine ON products("phenylalanine");
CREATE INDEX IF NOT EXISTS idx_products_name ON products("product_name");
