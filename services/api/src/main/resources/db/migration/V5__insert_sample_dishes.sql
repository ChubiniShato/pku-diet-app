-- V5__insert_sample_dishes.sql
-- Insert sample dishes for PKU diet planning demonstration

-- Note: This assumes some products exist from V3__insert_products_data.sql
-- We'll create a sample dish using hypothetical products

-- Insert sample dish: "PKU-Safe Breakfast Bowl"
INSERT INTO "dishes" (
    "id", "name", "category", "nominal_serving_grams", "manual_serving_override",
    "total_phenylalanine", "total_leucine", "total_tyrosine", "total_methionine",
    "total_kilojoules", "total_kilocalories", "total_protein", "total_carbohydrates", "total_fats",
    "per100_phenylalanine", "per100_leucine", "per100_tyrosine", "per100_methionine",
    "per100_kilojoules", "per100_kilocalories", "per100_protein", "per100_carbohydrates", "per100_fats"
) VALUES (
    'a1b2c3d4-e5f6-7890-abcd-123456789abc',
    'PKU-Safe Breakfast Bowl',
    'Breakfast',
    250.00, -- 250g total serving
    false,
    -- Total values for 250g serving
    45.50, 85.20, 32.10, 28.75, 
    1250.00, 298.50, 3.25, 58.40, 8.90,
    -- Per 100g values
    18.20, 34.08, 12.84, 11.50,
    500.00, 119.40, 1.30, 23.36, 3.56
);

-- Sample dish items (assuming we have products with these IDs)
-- Note: In practice, these product IDs would come from actual products in the products table
-- For demonstration, we'll use placeholder UUIDs

-- Item 1: Low-protein cereal (100g)
INSERT INTO "dish_items" (
    "id", "dish_id", "product_id", "grams",
    "snapshot_phenylalanine", "snapshot_leucine", "snapshot_tyrosine", "snapshot_methionine",
    "snapshot_kilojoules", "snapshot_kilocalories", "snapshot_protein", "snapshot_carbohydrates", "snapshot_fats"
) VALUES (
    'item-1111-2222-3333-444444444444',
    'a1b2c3d4-e5f6-7890-abcd-123456789abc',
    '11111111-2222-3333-4444-555555555555', -- Placeholder product ID
    100.00,
    15.00, 45.00, 18.00, 12.00,
    1400, 335, 1.20, 75.00, 2.50
);

-- Item 2: PKU-safe fruit (100g)
INSERT INTO "dish_items" (
    "id", "dish_id", "product_id", "grams",
    "snapshot_phenylalanine", "snapshot_leucine", "snapshot_tyrosine", "snapshot_methionine",
    "snapshot_kilojoules", "snapshot_kilocalories", "snapshot_protein", "snapshot_carbohydrates", "snapshot_fats"
) VALUES (
    'item-2222-3333-4444-555555555555',
    'a1b2c3d4-e5f6-7890-abcd-123456789abc',
    '22222222-3333-4444-5555-666666666666', -- Placeholder product ID
    100.00,
    8.50, 25.40, 12.20, 6.50,
    250, 60, 0.80, 14.50, 0.30
);

-- Item 3: Special low-protein milk (50g)
INSERT INTO "dish_items" (
    "id", "dish_id", "product_id", "grams",
    "snapshot_phenylalanine", "snapshot_leucine", "snapshot_tyrosine", "snapshot_methionine",
    "snapshot_kilojoules", "snapshot_kilocalories", "snapshot_protein", "snapshot_carbohydrates", "snapshot_fats"
) VALUES (
    'item-3333-4444-5555-666666666666',
    'a1b2c3d4-e5f6-7890-abcd-123456789abc',
    '33333333-4444-5555-6666-777777777777', -- Placeholder product ID
    50.00,
    44.00, 30.00, 25.00, 20.00,
    400, 95, 3.00, 4.50, 12.80
);

-- Insert another sample dish: "PKU Lunch Salad"
INSERT INTO "dishes" (
    "id", "name", "category", "nominal_serving_grams", "manual_serving_override",
    "total_phenylalanine", "total_leucine", "total_tyrosine", "total_methionine",
    "total_kilojoules", "total_kilocalories", "total_protein", "total_carbohydrates", "total_fats",
    "per100_phenylalanine", "per100_leucine", "per100_tyrosine", "per100_methionine",
    "per100_kilojoules", "per100_kilocalories", "per100_protein", "per100_carbohydrates", "per100_fats"
) VALUES (
    'b2c3d4e5-f6g7-8901-bcde-234567890def',
    'PKU Lunch Salad',
    'Lunch',
    180.00, -- 180g total serving
    false,
    -- Total values for 180g serving
    25.20, 48.60, 18.90, 15.30,
    720.00, 172.00, 1.80, 32.40, 4.50,
    -- Per 100g values
    14.00, 27.00, 10.50, 8.50,
    400.00, 95.56, 1.00, 18.00, 2.50
);

-- Add comment explaining the sample data
COMMENT ON TABLE "dishes" IS 'PKU diet dishes - meals composed of multiple products. Sample dishes included for demonstration.';

-- Note: In a real application, you would:
-- 1. First ensure the referenced products exist in the products table
-- 2. Use actual product IDs from your product catalog
-- 3. Calculate the nutritional values based on real product data
-- 4. This sample data is for demonstration purposes only
