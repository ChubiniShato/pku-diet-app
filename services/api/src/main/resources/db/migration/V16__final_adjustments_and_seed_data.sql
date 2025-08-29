-- V16__final_adjustments_and_seed_data.sql
-- Final adjustments and seed data for the PKU diet app

-- Create view for menu summary statistics
CREATE OR REPLACE VIEW "menu_day_summary" AS
SELECT 
    md.id,
    md.patient_id,
    md.date,
    md.status,
    COUNT(ms.id) as total_meal_slots,
    COUNT(CASE WHEN ms.is_consumed THEN 1 END) as consumed_meal_slots,
    SUM(ms.actual_phe_mg) as total_actual_phe_mg,
    SUM(ms.actual_protein_g) as total_actual_protein_g,
    SUM(ms.actual_kcal) as total_actual_kcal,
    SUM(ms.actual_fat_g) as total_actual_fat_g,
    COUNT(me.id) as total_menu_entries,
    COUNT(CASE WHEN me.is_consumed THEN 1 END) as consumed_menu_entries
FROM "menu_day" md
LEFT JOIN "meal_slot" ms ON md.id = ms.menu_day_id
LEFT JOIN "menu_entry" me ON ms.id = me.meal_slot_id
GROUP BY md.id, md.patient_id, md.date, md.status;

-- Create view for patient allergen summary
CREATE OR REPLACE VIEW "patient_allergen_summary" AS
SELECT 
    pp.id as patient_id,
    pp.name as patient_name,
    COUNT(pa.id) as total_allergens,
    COUNT(CASE WHEN pa.severity IN ('SEVERE', 'ANAPHYLACTIC') THEN 1 END) as severe_allergens,
    COUNT(pfi.id) as total_forbidden_ingredients,
    STRING_AGG(DISTINCT a.name, ', ' ORDER BY a.name) as allergen_names
FROM "patient_profile" pp
LEFT JOIN "patient_allergen" pa ON pp.id = pa.patient_id AND pa.is_active = true
LEFT JOIN "allergen" a ON pa.allergen_id = a.id
LEFT JOIN "patient_forbidden_ingredient" pfi ON pp.id = pfi.patient_id AND pfi.is_active = true
GROUP BY pp.id, pp.name;

-- Create view for pantry expiry alerts
CREATE OR REPLACE VIEW "pantry_expiry_alerts" AS
SELECT 
    pi.id,
    pi.patient_id,
    pp.name as patient_name,
    COALESCE(p.product_name, cp.name) as item_name,
    pi.quantity_grams,
    pi.expiry_date,
    pi.location,
    CASE 
        WHEN pi.expiry_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN pi.expiry_date <= CURRENT_DATE + INTERVAL '3 days' THEN 'EXPIRES_SOON'
        WHEN pi.expiry_date <= CURRENT_DATE + INTERVAL '7 days' THEN 'EXPIRES_WEEK'
        ELSE 'OK'
    END as expiry_status
FROM "pantry_item" pi
JOIN "patient_profile" pp ON pi.patient_id = pp.id
LEFT JOIN "products" p ON pi.product_id = p.id
LEFT JOIN "custom_product" cp ON pi.custom_product_id = cp.id
WHERE pi.is_available = true
    AND pi.expiry_date IS NOT NULL;

-- Insert sample patient profile for testing
INSERT INTO "patient_profile" (
    "id", "name", "birth_date", "weight_kg", "height_cm", "activity_level", "region"
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Test Patient',
    '1990-01-01',
    70.0,
    175.0,
    'MODERATE',
    'US'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample norm prescription
INSERT INTO "norm_prescription" (
    "patient_id", 
    "phe_limit_mg_per_day", 
    "protein_limit_g_per_day", 
    "kcal_min_per_day", 
    "fat_limit_g_per_day"
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    500.0,  -- 500mg PHE per day
    25.0,   -- 25g protein per day
    1800.0, -- 1800 kcal minimum
    65.0    -- 65g fat maximum
) ON CONFLICT ON CONSTRAINT "unique_active_prescription_per_patient" DO NOTHING;

-- Insert default notification preferences for the test patient
INSERT INTO "notification_preference" (
    "patient_id", "notification_type", "channel", "is_enabled", "frequency"
) VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'MENU_GENERATED', 'EMAIL', true, 'IMMEDIATE'),
    ('550e8400-e29b-41d4-a716-446655440000', 'LIMIT_BREACH', 'EMAIL', true, 'IMMEDIATE'),
    ('550e8400-e29b-41d4-a716-446655440000', 'PANTRY_EXPIRY', 'EMAIL', true, 'DAILY'),
    ('550e8400-e29b-41d4-a716-446655440000', 'MEAL_REMINDER', 'PUSH', false, 'IMMEDIATE')
ON CONFLICT ON CONSTRAINT "unique_patient_type_channel" DO NOTHING;

-- Insert some common allergens for the test patient (gluten sensitivity)
INSERT INTO "patient_allergen" (
    "patient_id", "allergen_id", "severity", "notes"
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    (SELECT id FROM "allergen" WHERE name = 'Gluten' LIMIT 1),
    'MODERATE',
    'Diagnosed with gluten sensitivity'
) ON CONFLICT ON CONSTRAINT "unique_patient_allergen" DO NOTHING;

-- Insert some sample pantry items
INSERT INTO "pantry_item" (
    "patient_id", "product_id", "item_type", "quantity_grams", 
    "purchase_date", "expiry_date", "location"
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    (SELECT id FROM "products" WHERE product_name = 'Potato' LIMIT 1),
    'PRODUCT',
    1000.0,
    CURRENT_DATE - INTERVAL '2 days',
    CURRENT_DATE + INTERVAL '10 days',
    'PANTRY'
),
(
    '550e8400-e29b-41d4-a716-446655440000',
    (SELECT id FROM "products" WHERE product_name = 'Spinach Leaves' LIMIT 1),
    'PRODUCT',
    200.0,
    CURRENT_DATE - INTERVAL '1 day',
    CURRENT_DATE + INTERVAL '3 days',
    'FRIDGE'
) ON CONFLICT DO NOTHING;

-- Create function to calculate menu entry nutritional values
CREATE OR REPLACE FUNCTION calculate_menu_entry_nutrition()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate nutritional values based on serving size and product data
    IF NEW.entry_type = 'PRODUCT' AND NEW.product_id IS NOT NULL THEN
        SELECT 
            (NEW.planned_serving_grams / 100.0) * p.phenylalanine,
            (NEW.planned_serving_grams / 100.0) * p.protein,
            (NEW.planned_serving_grams / 100.0) * p.kilocalories,
            (NEW.planned_serving_grams / 100.0) * p.fats
        INTO NEW.calculated_phe_mg, NEW.calculated_protein_g, NEW.calculated_kcal, NEW.calculated_fat_g
        FROM "products" p WHERE p.id = NEW.product_id;
        
    ELSIF NEW.entry_type = 'CUSTOM_PRODUCT' AND NEW.custom_product_id IS NOT NULL THEN
        SELECT 
            (NEW.planned_serving_grams / 100.0) * cp.phenylalanine,
            (NEW.planned_serving_grams / 100.0) * cp.protein,
            (NEW.planned_serving_grams / 100.0) * cp.kilocalories,
            (NEW.planned_serving_grams / 100.0) * cp.fats
        INTO NEW.calculated_phe_mg, NEW.calculated_protein_g, NEW.calculated_kcal, NEW.calculated_fat_g
        FROM "custom_product" cp WHERE cp.id = NEW.custom_product_id;
        
    ELSIF NEW.entry_type = 'DISH' AND NEW.dish_id IS NOT NULL THEN
        SELECT 
            (NEW.planned_serving_grams / d.nominal_serving_grams) * d.total_phenylalanine,
            (NEW.planned_serving_grams / d.nominal_serving_grams) * d.total_protein,
            (NEW.planned_serving_grams / d.nominal_serving_grams) * d.total_kilocalories,
            (NEW.planned_serving_grams / d.nominal_serving_grams) * d.total_fats
        INTO NEW.calculated_phe_mg, NEW.calculated_protein_g, NEW.calculated_kcal, NEW.calculated_fat_g
        FROM "dishes" d WHERE d.id = NEW.dish_id;
        
    ELSIF NEW.entry_type = 'CUSTOM_DISH' AND NEW.custom_dish_id IS NOT NULL THEN
        SELECT 
            (NEW.planned_serving_grams / cd.nominal_serving_grams) * cd.total_phenylalanine,
            (NEW.planned_serving_grams / cd.nominal_serving_grams) * cd.total_protein,
            (NEW.planned_serving_grams / cd.nominal_serving_grams) * cd.total_kilocalories,
            (NEW.planned_serving_grams / cd.nominal_serving_grams) * cd.total_fats
        INTO NEW.calculated_phe_mg, NEW.calculated_protein_g, NEW.calculated_kcal, NEW.calculated_fat_g
        FROM "custom_dish" cd WHERE cd.id = NEW.custom_dish_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic nutrition calculation
CREATE TRIGGER calculate_menu_entry_nutrition_trigger
    BEFORE INSERT OR UPDATE OF planned_serving_grams, actual_serving_grams
    ON "menu_entry"
    FOR EACH ROW
    EXECUTE FUNCTION calculate_menu_entry_nutrition();

-- Create function to update meal slot totals when menu entries change
CREATE OR REPLACE FUNCTION update_meal_slot_totals()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the meal slot totals
    UPDATE "meal_slot" SET
        actual_phe_mg = (
            SELECT COALESCE(SUM(calculated_phe_mg), 0)
            FROM "menu_entry" 
            WHERE meal_slot_id = COALESCE(NEW.meal_slot_id, OLD.meal_slot_id)
        ),
        actual_protein_g = (
            SELECT COALESCE(SUM(calculated_protein_g), 0)
            FROM "menu_entry" 
            WHERE meal_slot_id = COALESCE(NEW.meal_slot_id, OLD.meal_slot_id)
        ),
        actual_kcal = (
            SELECT COALESCE(SUM(calculated_kcal), 0)
            FROM "menu_entry" 
            WHERE meal_slot_id = COALESCE(NEW.meal_slot_id, OLD.meal_slot_id)
        ),
        actual_fat_g = (
            SELECT COALESCE(SUM(calculated_fat_g), 0)
            FROM "menu_entry" 
            WHERE meal_slot_id = COALESCE(NEW.meal_slot_id, OLD.meal_slot_id)
        ),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = COALESCE(NEW.meal_slot_id, OLD.meal_slot_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Create trigger for meal slot total updates
CREATE TRIGGER update_meal_slot_totals_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON "menu_entry"
    FOR EACH ROW
    EXECUTE FUNCTION update_meal_slot_totals();

-- Add comments to tables for documentation
COMMENT ON TABLE "patient_profile" IS 'Patient profiles with basic demographic and health information';
COMMENT ON TABLE "norm_prescription" IS 'Daily nutritional limits prescribed for each patient';
COMMENT ON TABLE "menu_week" IS 'Weekly menu plans for patients';
COMMENT ON TABLE "menu_day" IS 'Daily menu plans within weekly menus';
COMMENT ON TABLE "meal_slot" IS 'Individual meal slots within daily menus (breakfast, lunch, etc.)';
COMMENT ON TABLE "menu_entry" IS 'Individual food items within meal slots';
COMMENT ON TABLE "allergen" IS 'Master list of common allergens';
COMMENT ON TABLE "patient_allergen" IS 'Patient-specific allergen associations';
COMMENT ON TABLE "pantry_item" IS 'Available ingredients in patient pantries';
-- COMMENT ON TABLE "share_link" IS 'Shareable links for menu and health data'; -- Table not created yet
-- COMMENT ON TABLE "notification" IS 'System notifications for patients'; -- Table not created yet

-- Create indexes for performance on calculated columns
CREATE INDEX IF NOT EXISTS idx_menu_entry_calculated_phe ON "menu_entry"("calculated_phe_mg");
CREATE INDEX IF NOT EXISTS idx_meal_slot_actual_phe ON "meal_slot"("actual_phe_mg");
CREATE INDEX IF NOT EXISTS idx_menu_day_total_phe ON "menu_day"("total_day_phe_mg");


