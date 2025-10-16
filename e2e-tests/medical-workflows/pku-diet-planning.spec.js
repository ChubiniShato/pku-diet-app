/**
 * PKU Diet Planning E2E Tests
 * Tests critical medical workflows for PKU patients
 */

const { test, expect } = require('@playwright/test');

test.describe('PKU Diet Planning Workflows', () => {
  
  test.beforeEach(async ({ page }) => {
    // Navigate to the app and login with demo credentials
    await page.goto('/');
    await page.fill('[data-testid="username-input"]', 'admin');
    await page.fill('[data-testid="password-input"]', 'admin123');
    await page.click('[data-testid="sign-in-button"]');
    
    // Wait for successful login
    await expect(page.locator('[data-testid="dashboard-title"]')).toBeVisible();
  });

  test('Complete PKU Diet Planning Journey', async ({ page }) => {
    // Step 1: Navigate to menu generation
    await page.click('[data-testid="menu-generation-nav"]');
    await expect(page.locator('[data-testid="menu-generator-title"]')).toBeVisible();

    // Step 2: Select patient profile
    await page.selectOption('[data-testid="patient-select"]', 'demo-patient-1');
    
    // Step 3: Configure dietary restrictions
    await page.fill('[data-testid="phe-limit-input"]', '300'); // mg per day
    await page.fill('[data-testid="protein-limit-input"]', '20'); // grams per day
    await page.fill('[data-testid="calorie-target-input"]', '1800'); // calories per day

    // Step 4: Generate daily menu
    await page.click('[data-testid="generate-daily-menu-button"]');
    
    // Wait for menu generation (medical calculations take time)
    await expect(page.locator('[data-testid="generated-menu"]')).toBeVisible({ timeout: 30000 });
    
    // Step 5: Validate nutritional compliance
    await expect(page.locator('[data-testid="phe-total"]')).toContainText(/≤ 300 mg/);
    await expect(page.locator('[data-testid="protein-total"]')).toContainText(/≤ 20 g/);
    
    // Step 6: Review meal suggestions
    await expect(page.locator('[data-testid="breakfast-meal"]')).toBeVisible();
    await expect(page.locator('[data-testid="lunch-meal"]')).toBeVisible();
    await expect(page.locator('[data-testid="dinner-meal"]')).toBeVisible();
    
    // Step 7: Validate PKU-friendly foods only
    const mealItems = page.locator('[data-testid="meal-item"]');
    const itemCount = await mealItems.count();
    
    for (let i = 0; i < itemCount; i++) {
      const item = mealItems.nth(i);
      await expect(item.locator('[data-testid="phe-content"]')).toContainText(/≤ \d+ mg/);
    }
  });

  test('Nutritional Validation Workflow', async ({ page }) => {
    // Navigate to validation section
    await page.click('[data-testid="validation-nav"]');
    
    // Add custom meal for validation
    await page.click('[data-testid="add-custom-meal-button"]');
    
    // Input meal data
    await page.fill('[data-testid="meal-name-input"]', 'Test PKU Meal');
    await page.fill('[data-testid="phe-content-input"]', '150'); // mg
    await page.fill('[data-testid="protein-content-input"]', '8'); // grams
    await page.fill('[data-testid="calories-input"]', '400'); // calories
    
    // Validate meal
    await page.click('[data-testid="validate-meal-button"]');
    
    // Check validation results
    await expect(page.locator('[data-testid="validation-status"]')).toContainText('Compliant');
    await expect(page.locator('[data-testid="phe-status"]')).toContainText('Within limits');
    await expect(page.locator('[data-testid="protein-status"]')).toContainText('Within limits');
  });

  test('Patient Profile Management', async ({ page }) => {
    // Navigate to patient management
    await page.click('[data-testid="patients-nav"]');
    
    // Create new patient profile
    await page.click('[data-testid="add-patient-button"]');
    
    // Fill patient information
    await page.fill('[data-testid="patient-name-input"]', 'Test Patient');
    await page.fill('[data-testid="patient-age-input"]', '25');
    await page.selectOption('[data-testid="patient-gender-select"]', 'female');
    
    // Set PKU-specific parameters
    await page.fill('[data-testid="phe-tolerance-input"]', '250'); // mg/day
    await page.fill('[data-testid="protein-requirement-input"]', '18'); // g/day
    await page.fill('[data-testid="calorie-requirement-input"]', '2000'); // cal/day
    
    // Add dietary restrictions
    await page.check('[data-testid="lactose-intolerant-checkbox"]');
    await page.check('[data-testid="gluten-free-checkbox"]');
    
    // Save patient profile
    await page.click('[data-testid="save-patient-button"]');
    
    // Verify patient was created
    await expect(page.locator('[data-testid="patient-list"]')).toContainText('Test Patient');
    await expect(page.locator('[data-testid="phe-tolerance-display"]')).toContainText('250 mg/day');
  });

  test('Food Product Database Search', async ({ page }) => {
    // Navigate to products section
    await page.click('[data-testid="products-nav"]');
    
    // Search for PKU-friendly foods
    await page.fill('[data-testid="product-search-input"]', 'apple');
    await page.click('[data-testid="search-button"]');
    
    // Verify search results
    await expect(page.locator('[data-testid="product-results"]')).toBeVisible();
    
    // Check nutritional information is displayed
    const firstProduct = page.locator('[data-testid="product-item"]').first();
    await expect(firstProduct.locator('[data-testid="phe-content"]')).toBeVisible();
    await expect(firstProduct.locator('[data-testid="protein-content"]')).toBeVisible();
    await expect(firstProduct.locator('[data-testid="calories-content"]')).toBeVisible();
    
    // Filter by PKU-friendly category
    await page.selectOption('[data-testid="category-filter"]', 'pkufriendly');
    
    // Verify all results are PKU-friendly
    const productItems = page.locator('[data-testid="product-item"]');
    const count = await productItems.count();
    
    for (let i = 0; i < count; i++) {
      const item = productItems.nth(i);
      const pheContent = await item.locator('[data-testid="phe-content"]').textContent();
      const pheValue = parseFloat(pheContent.match(/\d+/)[0]);
      
      // PKU-friendly foods should have low PHE content
      expect(pheValue).toBeLessThanOrEqual(50); // mg per 100g
    }
  });

  test('Emergency PKU Guidelines Display', async ({ page }) => {
    // Navigate to help section
    await page.click('[data-testid="help-nav"]');
    
    // Check emergency guidelines are visible
    await expect(page.locator('[data-testid="emergency-guidelines"]')).toBeVisible();
    
    // Verify critical information is present
    await expect(page.locator('[data-testid="phe-limit-info"]')).toContainText(/300 mg/);
    await expect(page.locator('[data-testid="symptoms-info"]')).toContainText('neurological');
    
    // Test emergency contact information
    await expect(page.locator('[data-testid="emergency-contact"]')).toBeVisible();
    await expect(page.locator('[data-testid="medical-professional-contact"]')).toBeVisible();
  });

  test('Data Export for Medical Records', async ({ page }) => {
    // Navigate to patient profile
    await page.click('[data-testid="patients-nav"]');
    await page.click('[data-testid="patient-item"]:has-text("Demo Patient")');
    
    // Export patient data
    await page.click('[data-testid="export-patient-data-button"]');
    
    // Wait for download to start
    const downloadPromise = page.waitForEvent('download');
    await page.click('[data-testid="confirm-export-button"]');
    const download = await downloadPromise;
    
    // Verify file type and name
    expect(download.suggestedFilename()).toMatch(/patient-data-.*\.json/);
    
    // Verify file contains medical data
    const fileContent = await download.createReadStream();
    const content = await new Promise((resolve) => {
      let data = '';
      fileContent.on('data', chunk => data += chunk);
      fileContent.on('end', () => resolve(data));
    });
    
    const jsonContent = JSON.parse(content);
    expect(jsonContent).toHaveProperty('patientId');
    expect(jsonContent).toHaveProperty('pheLimits');
    expect(jsonContent).toHaveProperty('nutritionalHistory');
  });
});
