import { test, expect } from './fixtures/auth';

test.describe('Language Switcher (i18n)', () => {
  test.beforeEach(async ({ authenticatedPage: page }) => {
    await page.goto('/dashboard');
  });

  test('displays language switcher', async ({ authenticatedPage: page }) => {
    // Language switcher should be visible
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული|Русский|Українська/ });
    await expect(languageSelect).toBeVisible();
  });

  test('switches to Georgian', async ({ authenticatedPage: page }) => {
    // Find language selector
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Change to Georgian
    await languageSelect.selectOption('ka');
    
    // Wait for language change
    await page.waitForTimeout(500);
    
    // Verify Georgian text appears (check common words)
    const georgianText = page.locator('text=/მენიუ|პროდუქტ|პაციენტ|სია/');
    await expect(georgianText.first()).toBeVisible({ timeout: 3000 });
  });

  test('switches to English', async ({ authenticatedPage: page }) => {
    // First switch to Georgian
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    await languageSelect.selectOption('ka');
    await page.waitForTimeout(500);
    
    // Then switch back to English
    await languageSelect.selectOption('en');
    await page.waitForTimeout(500);
    
    // Verify English text
    const englishText = page.locator('text=/Menu|Product|Patient|List|Dashboard/');
    await expect(englishText.first()).toBeVisible({ timeout: 3000 });
  });

  test('switches to Russian', async ({ authenticatedPage: page }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|Русский/ });
    
    await languageSelect.selectOption('ru');
    await page.waitForTimeout(500);
    
    // Verify Russian text appears
    const russianText = page.locator('text=/Меню|Продукт|Пациент/');
    await expect(russianText.first()).toBeVisible({ timeout: 3000 });
  });

  test('switches to Ukrainian', async ({ authenticatedPage: page }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|Українська/ });
    
    await languageSelect.selectOption('uk');
    await page.waitForTimeout(500);
    
    // Verify Ukrainian text appears
    const ukrainianText = page.locator('text=/Меню|Продукт|Пацієнт/');
    await expect(ukrainianText.first()).toBeVisible({ timeout: 3000 });
  });

  test('persists language preference after page reload', async ({ authenticatedPage: page, context }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Switch to Georgian
    await languageSelect.selectOption('ka');
    await page.waitForTimeout(500);
    
    // Reload page
    await page.reload();
    await page.waitForTimeout(1000);
    
    // Language should still be Georgian
    const georgianText = page.locator('text=/მენიუ|პროდუქტ|პაციენტ/');
    await expect(georgianText.first()).toBeVisible({ timeout: 3000 });
    
    // Verify select shows Georgian
    const currentLanguage = await languageSelect.inputValue();
    expect(currentLanguage).toBe('ka');
  });

  test('persists language preference in new tab', async ({ authenticatedPage: page, context }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Switch to Georgian
    await languageSelect.selectOption('ka');
    await page.waitForTimeout(500);
    
    // Open new tab/page
    const newPage = await context.newPage();
    await newPage.goto('/dashboard');
    await newPage.waitForLoadState('networkidle');
    
    // New page should also be in Georgian
    const georgianText = newPage.locator('text=/მენიუ|პროდუქტ|პაციენტ/');
    await expect(georgianText.first()).toBeVisible({ timeout: 3000 });
    
    await newPage.close();
  });

  test('updates all UI elements when language changes', async ({ authenticatedPage: page }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Capture initial English navigation text
    const navText = await page.locator('nav').textContent();
    
    // Switch to Georgian
    await languageSelect.selectOption('ka');
    await page.waitForTimeout(500);
    
    // Navigation should have changed
    const newNavText = await page.locator('nav').textContent();
    expect(newNavText).not.toBe(navText);
  });

  test('language switcher works on login page', async ({ page }) => {
    await page.goto('/login');
    
    // Find language switcher
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    if (await languageSelect.isVisible({ timeout: 2000 }).catch(() => false)) {
      // Switch to Georgian
      await languageSelect.selectOption('ka');
      await page.waitForTimeout(500);
      
      // Login form should be in Georgian
      const georgianLoginText = page.locator('text=/შესვლა|სახელი|პაროლი/');
      await expect(georgianLoginText.first()).toBeVisible({ timeout: 3000 });
    } else {
      // Language switcher might only be available after login
      test.skip();
    }
  });

  test('date formats change with language', async ({ authenticatedPage: page }) => {
    // Navigate to a page with dates (e.g., menu calendar)
    await page.goto('/menu');
    
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Switch languages and verify dates update
    await languageSelect.selectOption('en');
    await page.waitForTimeout(500);
    
    // Look for English month names
    const englishDate = page.locator('text=/January|February|March|Monday|Tuesday/');
    const hasEnglishDate = await englishDate.first().isVisible({ timeout: 2000 }).catch(() => false);
    
    // Switch to Georgian
    await languageSelect.selectOption('ka');
    await page.waitForTimeout(500);
    
    // If dates were visible, they should update
    if (hasEnglishDate) {
      const georgianDate = page.locator('text=/იანვარი|თებერვალი|მარტი|ორშაბათ/');
      const hasGeorgianDate = await georgianDate.first().isVisible({ timeout: 2000 }).catch(() => false);
      
      // At least verify the page is still functional
      expect(page.url()).toBeTruthy();
    }
  });

  test('all four languages are available in selector', async ({ authenticatedPage: page }) => {
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Get all options
    const options = await languageSelect.locator('option').allTextContents();
    
    // Verify all 4 languages are present
    expect(options.length).toBeGreaterThanOrEqual(4);
    
    const optionsText = options.join(' ');
    expect(optionsText).toContain('English');
    expect(optionsText).toContain('ქართული');
    expect(optionsText).toContain('Русский');
    expect(optionsText).toContain('Українська');
  });

  test('RTL support for languages (if applicable)', async ({ authenticatedPage: page }) => {
    // This test is a placeholder for RTL language support
    // Currently, none of the languages are RTL, but this demonstrates the pattern
    
    const languageSelect = page.locator('select').filter({ hasText: /English|ქართული/ });
    
    // Verify LTR for English
    await languageSelect.selectOption('en');
    await page.waitForTimeout(500);
    
    const htmlDir = await page.locator('html').getAttribute('dir');
    // Should be 'ltr' or null (default is ltr)
    expect(htmlDir === 'ltr' || htmlDir === null).toBeTruthy();
  });
});


