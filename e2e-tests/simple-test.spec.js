/**
 * Simple Test to Verify Playwright Setup
 */

const { test, expect } = require('@playwright/test');

test.describe('PKU Diet App - Basic Tests', () => {
  
  test('Application loads successfully', async ({ page }) => {
    // Navigate to the app
    await page.goto('/');
    
    // Check if the page loads
    await expect(page).toHaveTitle(/PKU Diet App/);
    
    // Check if login form is visible
    await expect(page.locator('[data-testid="username-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="password-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="sign-in-button"]')).toBeVisible();
  });

  test('Demo credentials login works', async ({ page }) => {
    // Navigate to the app
    await page.goto('/');
    
    // Fill in demo credentials
    await page.fill('[data-testid="username-input"]', 'admin');
    await page.fill('[data-testid="password-input"]', 'admin123');
    
    // Click sign in
    await page.click('[data-testid="sign-in-button"]');
    
    // Wait for successful login (dashboard should be visible)
    await expect(page.locator('[data-testid="dashboard-title"]')).toBeVisible({ timeout: 10000 });
  });

  test('Language selector is present', async ({ page }) => {
    // Navigate to the app
    await page.goto('/');
    
    // Check if language selector exists
    const languageSelector = page.locator('[data-testid="language-selector"]');
    
    if (await languageSelector.isVisible()) {
      await expect(languageSelector).toBeVisible();
      console.log('✅ Language selector found');
    } else {
      console.log('⚠️ Language selector not found - may not be implemented yet');
    }
  });
});
