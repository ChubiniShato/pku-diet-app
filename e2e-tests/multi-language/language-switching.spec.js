/**
 * Multi-language Testing for PKU Diet App
 * Tests Georgian, Russian, and English language switching
 */

const { test, expect } = require('@playwright/test');

test.describe('Multi-language Support', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Login with demo credentials
    await page.fill('[data-testid="username-input"]', 'admin');
    await page.fill('[data-testid="password-input"]', 'admin123');
    await page.click('[data-testid="sign-in-button"]');
    await expect(page.locator('[data-testid="dashboard-title"]')).toBeVisible();
  });

  test('Language Switcher Functionality', async ({ page }) => {
    // Test language switcher dropdown
    await page.click('[data-testid="language-selector"]');
    
    // Verify all languages are available
    await expect(page.locator('[data-testid="language-option-en"]')).toBeVisible();
    await expect(page.locator('[data-testid="language-option-ka"]')).toBeVisible();
    await expect(page.locator('[data-testid="language-option-ru"]')).toBeVisible();
  });

  test('English Language Display', async ({ page }) => {
    // Switch to English
    await page.selectOption('[data-testid="language-selector"]', 'en');
    
    // Verify English medical terminology
    await expect(page.locator('[data-testid="dashboard-title"]')).toContainText('Dashboard');
    await expect(page.locator('[data-testid="menu-generation-nav"]')).toContainText('Menu Generation');
    await expect(page.locator('[data-testid="phe-label"]')).toContainText('Phenylalanine');
    await expect(page.locator('[data-testid="protein-label"]')).toContainText('Protein');
  });

  test('Georgian Language Display', async ({ page }) => {
    // Switch to Georgian
    await page.selectOption('[data-testid="language-selector"]', 'ka');
    
    // Verify Georgian medical terminology
    await expect(page.locator('[data-testid="dashboard-title"]')).toContainText('დეშბორდი');
    await expect(page.locator('[data-testid="menu-generation-nav"]')).toContainText('მენიუს გენერაცია');
    await expect(page.locator('[data-testid="phe-label"]')).toContainText('ფენილალანინი');
    await expect(page.locator('[data-testid="protein-label"]')).toContainText('ცილა');
  });

  test('Russian Language Display', async ({ page }) => {
    // Switch to Russian
    await page.selectOption('[data-testid="language-selector"]', 'ru');
    
    // Verify Russian medical terminology
    await expect(page.locator('[data-testid="dashboard-title"]')).toContainText('Панель управления');
    await expect(page.locator('[data-testid="menu-generation-nav"]')).toContainText('Генерация меню');
    await expect(page.locator('[data-testid="phe-label"]')).toContainText('Фенилаланин');
    await expect(page.locator('[data-testid="protein-label"]')).toContainText('Белок');
  });

  test('Medical Terms Translation Accuracy', async ({ page }) => {
    const medicalTerms = {
      en: {
        'PKU': 'Phenylketonuria',
        'PHE': 'Phenylalanine',
        'Protein': 'Protein',
        'Calories': 'Calories'
      },
      ka: {
        'PKU': 'ფენილკეტონურია',
        'PHE': 'ფენილალანინი',
        'Protein': 'ცილა',
        'Calories': 'კალორია'
      },
      ru: {
        'PKU': 'Фенилкетонурия',
        'PHE': 'Фенилаланин',
        'Protein': 'Белок',
        'Calories': 'Калории'
      }
    };

    // Test each language
    for (const [langCode, terms] of Object.entries(medicalTerms)) {
      await page.selectOption('[data-testid="language-selector"]', langCode);
      
      // Verify each medical term is correctly translated
      for (const [key, expectedText] of Object.entries(terms)) {
        await expect(page.locator(`[data-testid="${key.toLowerCase()}-term"]`))
          .toContainText(expectedText);
      }
    }
  });

  test('Nutritional Data Display in All Languages', async ({ page }) => {
    // Navigate to products section
    await page.click('[data-testid="products-nav"]');
    
    const languages = ['en', 'ka', 'ru'];
    
    for (const lang of languages) {
      await page.selectOption('[data-testid="language-selector"]', lang);
      
      // Verify nutritional labels are translated
      await expect(page.locator('[data-testid="nutrition-label"]')).toBeVisible();
      
      // Verify units are correctly displayed
      const pheUnit = await page.locator('[data-testid="phe-unit"]').textContent();
      const proteinUnit = await page.locator('[data-testid="protein-unit"]').textContent();
      const calorieUnit = await page.locator('[data-testid="calorie-unit"]').textContent();
      
      // Units should be consistent across languages (mg, g, kcal)
      expect(pheUnit).toMatch(/mg|მგ/);
      expect(proteinUnit).toMatch(/g|გ/);
      expect(calorieUnit).toMatch(/kcal|კკალ|кал/);
    }
  });

  test('Error Messages Translation', async ({ page }) => {
    // Test validation errors in different languages
    await page.click('[data-testid="validation-nav"]');
    
    // Trigger validation error
    await page.fill('[data-testid="phe-content-input"]', '9999'); // Invalid high value
    await page.click('[data-testid="validate-meal-button"]');
    
    const languages = ['en', 'ka', 'ru'];
    const expectedErrors = {
      en: 'PHE limit exceeded',
      ka: 'ფენილალანინის ლიმიტი გადაჭარბებულია',
      ru: 'Превышен лимит фенилаланина'
    };
    
    for (const lang of languages) {
      await page.selectOption('[data-testid="language-selector"]', lang);
      
      await expect(page.locator('[data-testid="error-message"]'))
        .toContainText(expectedErrors[lang]);
    }
  });

  test('Help Documentation Language Switching', async ({ page }) => {
    await page.click('[data-testid="help-nav"]');
    
    const languages = ['en', 'ka', 'ru'];
    const helpTitles = {
      en: 'Help & Support',
      ka: 'დახმარება და მხარდაჭერა',
      ru: 'Помощь и поддержка'
    };
    
    for (const lang of languages) {
      await page.selectOption('[data-testid="language-selector"]', lang);
      
      await expect(page.locator('[data-testid="help-title"]'))
        .toContainText(helpTitles[lang]);
      
      // Verify medical guidelines are translated
      await expect(page.locator('[data-testid="medical-guidelines"]')).toBeVisible();
      await expect(page.locator('[data-testid="emergency-info"]')).toBeVisible();
    }
  });

  test('Language Persistence Across Navigation', async ({ page }) => {
    // Set language to Georgian
    await page.selectOption('[data-testid="language-selector"]', 'ka');
    
    // Navigate through different sections
    await page.click('[data-testid="products-nav"]');
    await expect(page.locator('[data-testid="products-title"]')).toContainText('პროდუქტები');
    
    await page.click('[data-testid="menu-nav"]');
    await expect(page.locator('[data-testid="menu-title"]')).toContainText('მენიუ');
    
    await page.click('[data-testid="patients-nav"]');
    await expect(page.locator('[data-testid="patients-title"]')).toContainText('პაციენტები');
    
    // Verify language is still Georgian
    await expect(page.locator('[data-testid="language-selector"]')).toHaveValue('ka');
  });
});
