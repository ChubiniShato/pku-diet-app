-- V28__remove_sample_dishes.sql
-- Remove sample dishes that are not needed

-- Delete the sample dishes that were created in V15
DELETE FROM "dishes" WHERE "id" = 'a1b2c3d4-e5f6-7890-abcd-123456789abc';
DELETE FROM "dishes" WHERE "id" = 'b2c3d4e5-f6a7-4901-bcde-234567890def';

-- Also delete any dish ingredients for these dishes
DELETE FROM "dish_ingredient" WHERE "dish_id" = 'a1b2c3d4-e5f6-7890-abcd-123456789abc';
DELETE FROM "dish_ingredient" WHERE "dish_id" = 'b2c3d4e5-f6a7-4901-bcde-234567890def';

-- Delete any dish allergens for these dishes
DELETE FROM "dish_allergen" WHERE "dish_id" = 'a1b2c3d4-e5f6-7890-abcd-123456789abc';
DELETE FROM "dish_allergen" WHERE "dish_id" = 'b2c3d4e5-f6a7-4901-bcde-234567890def';


