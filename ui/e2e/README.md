# E2E Tests with Playwright

## Overview

End-to-end tests for the PKU Diet App using Playwright. Tests cover critical user journeys as required by the project rules.

## Test Coverage

| Test Suite | Coverage | Status |
|------------|----------|--------|
| **auth.spec.ts** | Login, logout, validation, error handling | ✅ |
| **csv-upload.spec.ts** | CSV file upload, validation, size limits | ✅ |
| **menu-generation.spec.ts** | Daily/weekly menu generation, nutrition validation | ✅ |
| **i18n.spec.ts** | Language switcher (EN, KA, RU, UK), persistence | ✅ |

## Quick Start

### Install Dependencies

```bash
npm install
npx playwright install --with-deps chromium firefox
```

### Run Tests

```bash
# Run all E2E tests
npm run test:e2e

# Run in UI mode (interactive)
npm run test:e2e:ui

# Run in headed mode (see browser)
npm run test:e2e:headed

# Debug mode
npm run test:e2e:debug
```

### Run Specific Tests

```bash
# Run only auth tests
npx playwright test auth

# Run only CSV upload tests
npx playwright test csv-upload

# Run only menu generation tests
npx playwright test menu-generation

# Run only i18n tests
npx playwright test i18n
```

## Configuration

### Environment Variables

Create `.env` file in `ui/` directory:

```env
BASE_URL=http://localhost:5173
API_URL=http://localhost:8080
TEST_USER=admin
TEST_PASS=admin123
```

### Playwright Config

See `playwright.config.ts` for:
- Browser configurations (Chromium, Firefox)
- Retry logic (2 retries in CI)
- Video/screenshot on failure
- Test timeouts
- Web server auto-start

## Test Structure

```
e2e/
├── fixtures/
│   └── auth.ts           # Authenticated page fixture
├── helpers/
│   └── api.ts            # API helper functions
├── auth.spec.ts          # Authentication tests
├── csv-upload.spec.ts    # CSV upload tests
├── menu-generation.spec.ts # Menu generation tests
├── i18n.spec.ts          # Internationalization tests
└── README.md             # This file
```

## CI/CD Integration

Tests run automatically on:
- Pull requests touching `ui/**` or `services/api/**`
- Push to `main` or `develop` branches

See `.github/workflows/e2e.yml` for workflow details.

### CI Artifacts

- **Playwright Report**: HTML report with test results
- **Test Results**: Raw test output on failure
- **Screenshots**: Captured on test failure
- **Videos**: Recorded on test failure

## Writing New Tests

### Use Authenticated Fixture

```typescript
import { test, expect } from './fixtures/auth';

test('my test', async ({ authenticatedPage: page }) => {
  // page is already authenticated
  await page.goto('/dashboard');
  // ...
});
```

### API Helpers

```typescript
import { login, createPatient, createProduct } from './helpers/api';

test('test with API setup', async ({ request }) => {
  const token = await login(request, 'admin', 'admin123');
  const patient = await createPatient(request, token);
  // ...
});
```

## Best Practices

1. **Use data-testid attributes** for reliable selectors
2. **Wait for elements** with `expect().toBeVisible()` instead of `waitForTimeout()`
3. **Use fixtures** for common setup (authentication, test data)
4. **Keep tests independent** - each test should run in isolation
5. **Use API helpers** for test data setup instead of UI interactions
6. **Handle timing** with built-in waiters, avoid arbitrary timeouts
7. **Use test.skip()** for features not yet implemented

## Debugging

### Local Debugging

```bash
# Open Playwright Inspector
npm run test:e2e:debug

# Run with headed browsers
npm run test:e2e:headed

# UI mode (recommended)
npm run test:e2e:ui
```

### View Reports

```bash
# After test run
npx playwright show-report
```

### Screenshots & Videos

Located in:
- `test-results/` - Screenshots and videos from failed tests
- `playwright-report/` - HTML report with embedded artifacts

## Troubleshooting

### Tests failing locally but passing in CI

- Check environment variables
- Ensure API server is running
- Clear browser state: `npx playwright clean`

### Timeout errors

- Increase timeout in test: `test.setTimeout(60000)`
- Check if API is slow or down
- Verify network conditions

### Selector errors

- Use Playwright Inspector to debug selectors
- Add data-testid attributes for stability
- Use more specific selectors

## Integration with Project Rules

These tests fulfill the E2E requirements from `.cursor/rules/pku-diet-app-project-rules.mdc`:

- ✅ Login E2E tests
- ✅ CsvUpload E2E tests
- ✅ MenuGeneration E2E tests
- ✅ LanguageSwitcher E2E tests

All tests follow project conventions:
- Conventional commits
- Branch naming
- PR template requirements
- CI/CD blocking gates


