import { test as base } from '@playwright/test';

type AuthFixtures = {
  authenticatedPage: any;
};

export const test = base.extend<AuthFixtures>({
  authenticatedPage: async ({ page }, use) => {
    // Navigate to login page
    await page.goto('/login');
    
    // Perform login
    await page.fill('input[name="username"]', process.env.TEST_USER || 'admin');
    await page.fill('input[name="password"]', process.env.TEST_PASS || 'admin123');
    await page.click('button[type="submit"]');
    
    // Wait for successful login and navigation to dashboard
    await page.waitForURL('/dashboard', { timeout: 10000 });
    
    // Use the authenticated page
    await use(page);
  },
});

export { expect } from '@playwright/test';


