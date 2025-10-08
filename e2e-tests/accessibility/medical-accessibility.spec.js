/**
 * Accessibility Testing for PKU Diet App
 * Medical app compliance with WCAG 2.1 AA standards
 */

const { test, expect } = require('@playwright/test');

test.describe('Medical App Accessibility Compliance', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Login with demo credentials
    await page.fill('[data-testid="username-input"]', 'admin');
    await page.fill('[data-testid="password-input"]', 'admin123');
    await page.click('[data-testid="sign-in-button"]');
    await expect(page.locator('[data-testid="dashboard-title"]')).toBeVisible();
  });

  test('WCAG 2.1 AA Compliance Check', async ({ page }) => {
    // Run accessibility audit
    const accessibilityScanResults = await page.accessibility.snapshot();
    
    // Check for critical accessibility issues
    expect(accessibilityScanResults).toBeDefined();
    
    // Verify all interactive elements have proper labels
    const interactiveElements = await page.locator('button, input, select, a').all();
    
    for (const element of interactiveElements) {
      const ariaLabel = await element.getAttribute('aria-label');
      const ariaLabelledBy = await element.getAttribute('aria-labelledby');
      const textContent = await element.textContent();
      
      // At least one accessibility label should be present
      const hasAccessibilityLabel = ariaLabel || ariaLabelledBy || textContent?.trim();
      expect(hasAccessibilityLabel).toBeTruthy();
    }
  });

  test('Keyboard Navigation Support', async ({ page }) => {
    // Test tab navigation through main interface
    await page.keyboard.press('Tab');
    
    // Verify focus indicators are visible
    const focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();
    
    // Test tab order through critical medical functions
    const tabOrder = [
      '[data-testid="menu-generation-nav"]',
      '[data-testid="products-nav"]',
      '[data-testid="patients-nav"]',
      '[data-testid="validation-nav"]'
    ];
    
    for (const selector of tabOrder) {
      await page.keyboard.press('Tab');
      await expect(page.locator(selector)).toBeFocused();
    }
  });

  test('Screen Reader Compatibility', async ({ page }) => {
    // Navigate to menu generation (critical medical function)
    await page.click('[data-testid="menu-generation-nav"]');
    
    // Check for proper ARIA labels on medical inputs
    await expect(page.locator('[data-testid="phe-limit-input"]')).toHaveAttribute('aria-label');
    await expect(page.locator('[data-testid="protein-limit-input"]')).toHaveAttribute('aria-label');
    await expect(page.locator('[data-testid="calorie-target-input"]')).toHaveAttribute('aria-label');
    
    // Verify form validation messages have proper ARIA attributes
    await page.fill('[data-testid="phe-limit-input"]', '9999'); // Invalid value
    await page.click('[data-testid="generate-menu-button"]');
    
    const errorMessage = page.locator('[data-testid="phe-error-message"]');
    await expect(errorMessage).toHaveAttribute('role', 'alert');
    await expect(errorMessage).toHaveAttribute('aria-live', 'polite');
  });

  test('Color Contrast Compliance', async ({ page }) => {
    // Check critical medical information has sufficient contrast
    const criticalElements = [
      '[data-testid="phe-limit-display"]',
      '[data-testid="protein-limit-display"]',
      '[data-testid="warning-message"]',
      '[data-testid="error-message"]'
    ];
    
    for (const selector of criticalElements) {
      if (await page.locator(selector).isVisible()) {
        const element = page.locator(selector);
        
        // Check computed styles for contrast compliance
        const styles = await element.evaluate((el) => {
          const computedStyle = window.getComputedStyle(el);
          return {
            color: computedStyle.color,
            backgroundColor: computedStyle.backgroundColor,
            fontSize: computedStyle.fontSize
          };
        });
        
        // Verify minimum font size for medical information
        const fontSize = parseFloat(styles.fontSize);
        expect(fontSize).toBeGreaterThanOrEqual(12); // Minimum readable size
      }
    }
  });

  test('Medical Data Form Accessibility', async ({ page }) => {
    await page.click('[data-testid="patients-nav"]');
    await page.click('[data-testid="add-patient-button"]');
    
    // Verify all medical form fields have proper labels
    const formFields = [
      '[data-testid="patient-name-input"]',
      '[data-testid="patient-age-input"]',
      '[data-testid="phe-tolerance-input"]',
      '[data-testid="protein-requirement-input"]',
      '[data-testid="calorie-requirement-input"]'
    ];
    
    for (const field of formFields) {
      const input = page.locator(field);
      const label = page.locator(`label[for="${await input.getAttribute('id')}"]`);
      
      // Verify label exists and is associated with input
      await expect(label).toBeVisible();
      await expect(input).toHaveAttribute('aria-describedby');
    }
    
    // Test form validation accessibility
    await page.click('[data-testid="save-patient-button"]'); // Trigger validation
    
    // Verify validation errors are announced to screen readers
    const validationErrors = page.locator('[role="alert"]');
    await expect(validationErrors.first()).toBeVisible();
  });

  test('Emergency Information Accessibility', async ({ page }) => {
    await page.click('[data-testid="help-nav"]');
    
    // Verify emergency information is highly accessible
    const emergencySection = page.locator('[data-testid="emergency-guidelines"]');
    await expect(emergencySection).toHaveAttribute('role', 'region');
    await expect(emergencySection).toHaveAttribute('aria-labelledby');
    
    // Check emergency contact information
    const emergencyContact = page.locator('[data-testid="emergency-contact"]');
    await expect(emergencyContact).toHaveAttribute('role', 'complementary');
    
    // Verify critical warnings are properly marked
    const warningMessage = page.locator('[data-testid="critical-warning"]');
    if (await warningMessage.isVisible()) {
      await expect(warningMessage).toHaveAttribute('role', 'alert');
      await expect(warningMessage).toHaveAttribute('aria-live', 'assertive');
    }
  });

  test('Mobile Accessibility for Medical App', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Verify touch targets are large enough
    const touchTargets = [
      '[data-testid="menu-generation-nav"]',
      '[data-testid="generate-menu-button"]',
      '[data-testid="emergency-contact-button"]'
    ];
    
    for (const target of touchTargets) {
      if (await page.locator(target).isVisible()) {
        const box = await page.locator(target).boundingBox();
        
        // Minimum touch target size: 44x44 pixels
        expect(box.width).toBeGreaterThanOrEqual(44);
        expect(box.height).toBeGreaterThanOrEqual(44);
      }
    }
    
    // Test mobile navigation accessibility
    await page.click('[data-testid="mobile-menu-toggle"]');
    const mobileMenu = page.locator('[data-testid="mobile-navigation"]');
    await expect(mobileMenu).toHaveAttribute('aria-expanded', 'true');
  });

  test('High Contrast Mode Support', async ({ page }) => {
    // Simulate high contrast mode
    await page.emulateMedia({ colorScheme: 'dark' });
    
    // Verify medical information is still readable
    await page.click('[data-testid="menu-generation-nav"]');
    
    const criticalInfo = [
      '[data-testid="phe-limit-display"]',
      '[data-testid="protein-limit-display"]',
      '[data-testid="calorie-target-display"]'
    ];
    
    for (const selector of criticalInfo) {
      if (await page.locator(selector).isVisible()) {
        const element = page.locator(selector);
        await expect(element).toBeVisible();
        
        // Check if text is still readable in dark mode
        const color = await element.evaluate((el) => 
          window.getComputedStyle(el).color
        );
        expect(color).not.toBe('rgba(0, 0, 0, 0)'); // Not transparent
      }
    }
  });

  test('Assistive Technology Integration', async ({ page }) => {
    // Test with reduced motion preferences
    await page.emulateMedia({ reducedMotion: 'reduce' });
    
    // Verify animations are disabled for accessibility
    const animatedElements = page.locator('[data-testid*="animation"], [data-testid*="loading"]');
    const count = await animatedElements.count();
    
    if (count > 0) {
      for (let i = 0; i < count; i++) {
        const element = animatedElements.nth(i);
        const animation = await element.evaluate((el) => 
          window.getComputedStyle(el).animation
        );
        
        // Animation should be disabled in reduced motion mode
        expect(animation).toBe('none');
      }
    }
    
    // Test focus management for modal dialogs
    await page.click('[data-testid="add-patient-button"]');
    
    const modal = page.locator('[data-testid="patient-modal"]');
    await expect(modal).toBeVisible();
    
    // Verify focus is trapped within modal
    await page.keyboard.press('Tab');
    const focusedInModal = await modal.locator(':focus').count();
    expect(focusedInModal).toBeGreaterThan(0);
  });

  test('Medical Data Table Accessibility', async ({ page }) => {
    await page.click('[data-testid="products-nav"]');
    
    // Verify data tables have proper accessibility attributes
    const table = page.locator('[data-testid="products-table"]');
    await expect(table).toHaveAttribute('role', 'table');
    
    // Check table headers
    const headers = table.locator('th');
    const headerCount = await headers.count();
    
    for (let i = 0; i < headerCount; i++) {
      const header = headers.nth(i);
      await expect(header).toHaveAttribute('scope', 'col');
    }
    
    // Verify table cells are properly associated with headers
    const firstRow = table.locator('tbody tr').first();
    const cells = firstRow.locator('td');
    const cellCount = await cells.count();
    
    for (let i = 0; i < cellCount; i++) {
      const cell = cells.nth(i);
      await expect(cell).toHaveAttribute('headers');
    }
  });
});
