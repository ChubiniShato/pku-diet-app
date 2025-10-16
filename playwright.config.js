/**
 * Playwright Configuration for PKU Diet App
 * Medical-grade E2E testing with accessibility compliance
 */

const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './e2e-tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/results.xml' }]
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    // Medical app specific settings
    viewport: { width: 1280, height: 720 },
    ignoreHTTPSErrors: true,
    // Accessibility testing
    accessibility: {
      enabled: true,
      rules: ['wcag2a', 'wcag2aa']
    }
  },
  
  projects: [
    {
      name: 'medical-workflows',
      testMatch: '**/medical-workflows/**/*.spec.js',
      use: {
        // Medical app specific browser settings
        userAgent: 'PKU-Diet-App-Test/1.0',
        extraHTTPHeaders: {
          'X-Test-Environment': 'medical-e2e'
        }
      }
    },
    {
      name: 'accessibility',
      testMatch: '**/accessibility/**/*.spec.js',
      use: {
        // Accessibility testing configuration
        accessibility: {
          enabled: true,
          rules: ['wcag2a', 'wcag2aa', 'wcag2aaa']
        }
      }
    },
    {
      name: 'multi-language',
      testMatch: '**/multi-language/**/*.spec.js',
      use: {
        // Multi-language testing
        locale: 'en-US',
        timezoneId: 'UTC'
      }
    },
    {
      name: 'performance',
      testMatch: '**/performance/**/*.spec.js',
      use: {
        // Performance testing settings
        trace: 'on',
        video: 'on'
      }
    }
  ],

  // Medical compliance testing
  expect: {
    // Medical app specific assertions
    timeout: 10000,
    toHaveScreenshot: { threshold: 0.2 },
    toMatchSnapshot: { threshold: 0.2 }
  },

  // Web server configuration (disabled - start services manually)
  // webServer: {
  //   command: 'cd ui && npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: !process.env.CI,
  //   timeout: 120 * 1000
  // },

  // Global setup for medical data
  globalSetup: require.resolve('./e2e-tests/global-setup.js'),
  
  // Global teardown
  globalTeardown: require.resolve('./e2e-tests/global-teardown.js'),

  // Medical app specific output directory
  outputDir: 'test-results/',
  
  // Test timeout for medical workflows (longer for complex calculations)
  timeout: 60000,
  
  // Expect timeout for medical validations
  expect: {
    timeout: 30000
  }
});
