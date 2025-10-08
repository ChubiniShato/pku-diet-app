# 🧪 PKU Diet App - E2E Testing Suite

## 📋 Overview

E2E (End-to-End) testing suite using **Playwright** for medical-grade testing of PKU Diet App workflows.

---

## 🎯 Test Coverage

### ✅ Medical Workflows
- Complete PKU diet planning journey
- Nutritional validation workflow
- Patient profile management
- Food product database search
- Emergency PKU guidelines
- Medical records export

### ♿ Accessibility Testing
- WCAG 2.1 AA compliance
- Keyboard navigation
- Screen reader support
- Color contrast validation
- Focus management

### 🌍 Multi-language Testing
- Georgian (ka)
- Russian (ru)
- English (en)
- Language switcher functionality
- Medical terminology translation

---

## 🚀 Quick Start

### 1. Install Dependencies

```bash
npm install
npx playwright install
```

### 2. Start Application

**Option A: Docker Compose**
```bash
# Start API + Database
docker-compose up -d

# Start UI
cd ui && docker-compose up -d
```

**Option B: Manual**
```bash
# Terminal 1: Start backend
cd services/api
mvn spring-boot:run

# Terminal 2: Start frontend
cd ui
npm run dev
```

### 3. Run Tests

```bash
# All tests
npm run test:e2e

# Specific test suites
npm run test:medical          # Medical workflows
npm run test:accessibility    # Accessibility tests
npm run test:multilang        # Multi-language tests
npm run test:performance      # Performance tests

# Interactive UI mode
npm run test:e2e:ui

# Debug mode
npm run test:e2e:debug

# Headed mode (see browser)
npm run test:e2e:headed
```

### 4. View Reports

```bash
npm run test:report
```

---

## 📁 Test Structure

```
e2e-tests/
├── medical-workflows/
│   └── pku-diet-planning.spec.js    # Core medical workflows
├── accessibility/
│   └── medical-accessibility.spec.js # WCAG compliance
├── multi-language/
│   └── language-switching.spec.js   # i18n testing
├── global-setup.js                  # Test environment setup
└── global-teardown.js               # Cleanup
```

---

## 🔧 Configuration

### Environment Variables

```bash
# Base URL (default: http://localhost:5173)
BASE_URL=http://localhost:5173

# CI mode (affects retries and parallelism)
CI=true
```

### Test Data

Tests use demo credentials:
- **Username**: `admin`
- **Password**: `admin123`

⚠️ **Note**: No real patient data is used in tests.

---

## 📊 Test Reports

### Generated Artifacts

- **HTML Report**: `test-results/index.html`
- **JSON Report**: `test-results/results.json`
- **JUnit Report**: `test-results/results.xml` (for CI/CD)
- **Screenshots**: Captured on failure
- **Videos**: Recorded on failure

### View Reports

```bash
npm run test:report
# Opens HTML report in browser
```

---

## 🏥 Medical Compliance

### Data Privacy
- ✅ Synthetic test data only
- ✅ No real patient information
- ✅ Isolated test environment
- ✅ HIPAA-compliant practices

### Quality Assurance
- ✅ Medical terminology validation
- ✅ Nutritional calculation accuracy
- ✅ PKU-specific workflow verification
- ✅ Emergency procedure testing

---

## 🐛 Troubleshooting

### Application Not Running

```bash
# Check API health
curl http://localhost:8080/actuator/health

# Check UI
curl http://localhost:5173
```

### Playwright Browsers Missing

```bash
npx playwright install
```

### Clear Test Data

```bash
# Stop all services
docker-compose down

# Remove volumes
docker-compose down -v

# Restart
docker-compose up -d
```

### Tests Failing

1. Ensure application is running
2. Check `data-testid` attributes match
3. Review screenshots in `test-results/`
4. Run in debug mode: `npm run test:e2e:debug`

---

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
- name: Install dependencies
  run: npm install

- name: Install Playwright browsers
  run: npx playwright install --with-deps

- name: Run E2E tests
  run: npm run test:e2e
  env:
    CI: true
    BASE_URL: http://localhost:5173

- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-report
    path: test-results/
```

---

## 📚 Best Practices

1. **Data-testid Attributes**: Always use `data-testid` for test selectors
2. **Timeouts**: Medical calculations may take longer - adjust timeouts
3. **Screenshots**: Review screenshots on failures
4. **Isolation**: Each test should be independent
5. **Cleanup**: Use `globalTeardown` for proper cleanup

---

## 🤝 Contributing

When adding new tests:

1. Follow existing test structure
2. Use descriptive test names
3. Add comments for medical workflows
4. Ensure accessibility compliance
5. Test in all supported languages
6. Document test scenarios

---

## 📞 Support

For issues:
- Check troubleshooting section
- Review test logs in `test-results/`
- Verify application is running
- Check Playwright documentation: https://playwright.dev/

---

**🎉 Happy Testing!**

