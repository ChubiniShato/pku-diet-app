# ⚡ E2E Testing - Quick Start

## 🎯 Prerequisites

1. **Application Running**
   ```bash
   # Backend API
   http://localhost:8080 ✅
   
   # Frontend UI  
   http://localhost:5173 ✅
   ```

2. **Node.js & Dependencies**
   ```bash
   cd c:\dev\pku-diet-app
   npm install
   npx playwright install chromium
   ```

---

## 🚀 Run Tests

### All Tests
```bash
npm run test:e2e
```

### By Category
```bash
npm run test:medical          # Medical workflows
npm run test:accessibility    # Accessibility tests
npm run test:multilang        # Multi-language tests
```

### Interactive Mode
```bash
npm run test:e2e:ui           # Visual test runner
npm run test:e2e:debug        # Debug mode
npm run test:e2e:headed       # See browser
```

### View Reports
```bash
npm run test:report
```

---

## ⚠️ Important Notes

1. **Tests expect specific `data-testid` attributes in UI components**
   - If tests fail, verify UI components have matching IDs
   - Example: `<button data-testid="sign-in-button">Sign In</button>`

2. **Demo credentials used in tests:**
   - Username: `admin`
   - Password: `admin123`

3. **Tests are READ from archive - may need updates**
   - Verify against current UI implementation
   - Update selectors if UI changed

---

## 📁 What Was Migrated

✅ **From Archive:**
- `e2e-tests/` directory (all tests)
- `playwright.config.js` (Playwright configuration)
- `package.json` (root - E2E scripts)
- `E2E_TESTING_README.md` (full documentation)

❌ **Not Migrated:**
- `context7-integration.js` (not real MCP server)
- `context7-config.json` (misleading name)

---

## 🔧 Troubleshooting

### Tests immediately fail
→ **Check if application is running**
```bash
curl http://localhost:5173
curl http://localhost:8080/actuator/health
```

### "Cannot find data-testid" errors
→ **UI components missing test IDs**
- Review test files in `e2e-tests/`
- Add missing `data-testid` attributes to UI
- Or update test selectors

### Chromium not installed
```bash
npx playwright install chromium
```

---

## 📚 Full Documentation

See [E2E_TESTING_README.md](./E2E_TESTING_README.md) for complete guide.

---

**✅ Ready to test!**

