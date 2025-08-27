-- V7__insert_sample_dishes_data.sql
-- Insert sample dishes for PKU diet planning demonstration

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
) ON CONFLICT (id) DO NOTHING;

-- Insert another sample dish: "PKU Lunch Salad"
INSERT INTO "dishes" (
    "id", "name", "category", "nominal_serving_grams", "manual_serving_override",
    "total_phenylalanine", "total_leucine", "total_tyrosine", "total_methionine",
    "total_kilojoules", "total_kilocalories", "total_protein", "total_carbohydrates", "total_fats",
    "per100_phenylalanine", "per100_leucine", "per100_tyrosine", "per100_methionine",
    "per100_kilojoules", "per100_kilocalories", "per100_protein", "per100_carbohydrates", "per100_fats"
) VALUES (
    'b2c3d4e5-f6a7-4901-bcde-234567890def',
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
) ON CONFLICT (id) DO NOTHING;

-- Add comment explaining the sample data
COMMENT ON TABLE "dishes" IS 'PKU diet dishes - meals composed of multiple products. Sample dishes included for demonstration.';
