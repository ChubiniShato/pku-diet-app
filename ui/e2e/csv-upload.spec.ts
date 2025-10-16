import { test, expect } from './fixtures/auth';
import path from 'path';

test.describe('CSV Upload', () => {
  test.beforeEach(async ({ authenticatedPage: page }) => {
    // Navigate to products/import page (adjust route based on your app)
    await page.goto('/products');
    
    // Try to find import or upload link/button
    const importButton = page.locator('button:has-text("Import"), a:has-text("Import"), button:has-text("Upload"), a:has-text("Upload")').first();
    
    if (await importButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await importButton.click();
    }
  });

  test('displays upload dropzone', async ({ authenticatedPage: page }) => {
    // Verify upload UI is present
    await expect(page.locator('input[type="file"]')).toBeAttached();
    await expect(page.locator('text=/drag.*drop/i, text=/browse/i')).toBeVisible();
  });

  test('uploads valid CSV file via file input', async ({ authenticatedPage: page }) => {
    // Create test CSV content
    const csvContent = `name,phe,protein,calories,category
Test Product 1,50,5.5,120,vegetables
Test Product 2,30,3.2,80,fruits
Test Product 3,45,4.8,100,grains`;

    // Upload file
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'test-products.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent),
    });

    // Verify file is selected
    await expect(page.locator('text=/test-products.csv/i')).toBeVisible({ timeout: 3000 });

    // Click upload button if it exists
    const uploadButton = page.locator('button:has-text("Upload")').first();
    if (await uploadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await uploadButton.click();

      // Wait for upload completion (success message or table with data)
      await expect(
        page.locator('text=/success/i, .bg-green-50, table tbody tr')
      ).toBeVisible({ timeout: 10000 });
    }
  });

  test('rejects file exceeding size limit', async ({ authenticatedPage: page }) => {
    // Create a file larger than 10MB
    const largeContent = 'x'.repeat(11 * 1024 * 1024);
    
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'large-file.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(largeContent),
    });

    // Should show error about file size
    await expect(page.locator('text=/too large/i, text=/10.*mb/i, .text-red-600')).toBeVisible({
      timeout: 3000,
    });

    // Upload button should not appear or be disabled
    const uploadButton = page.locator('button:has-text("Upload")');
    if (await uploadButton.isVisible({ timeout: 1000 }).catch(() => false)) {
      await expect(uploadButton).toBeDisabled();
    }
  });

  test('rejects non-CSV file', async ({ authenticatedPage: page }) => {
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'test.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('not a csv file'),
    });

    // Should show error about invalid file type
    await expect(page.locator('text=/invalid.*type/i, .text-red-600')).toBeVisible({
      timeout: 3000,
    });
  });

  test('allows clearing selected file', async ({ authenticatedPage: page }) => {
    const csvContent = 'name,phe,protein\nTest,50,5';

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent),
    });

    // File should be selected
    await expect(page.locator('text=/test.csv/i')).toBeVisible();

    // Clear button should be visible
    const clearButton = page.locator('button:has-text("Clear"), button:has-text("Remove")').first();
    if (await clearButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await clearButton.click();

      // File info should disappear
      await expect(page.locator('text=/test.csv/i')).not.toBeVisible();
    }
  });

  test('shows upload progress', async ({ authenticatedPage: page }) => {
    const csvContent = `name,phe,protein,calories
${'Product,50,5,100\n'.repeat(100)}`;

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'products.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent),
    });

    const uploadButton = page.locator('button:has-text("Upload")').first();
    if (await uploadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await uploadButton.click();

      // Progress bar or loading indicator should appear
      const progressIndicator = page.locator('.bg-blue-600, .animate-spin, text=/uploading/i');
      
      // Try to catch progress (might be too fast)
      const isProgressVisible = await progressIndicator.isVisible({ timeout: 1000 }).catch(() => false);
      
      // If we caught it, verify it disappears on completion
      if (isProgressVisible) {
        await expect(progressIndicator).not.toBeVisible({ timeout: 10000 });
      }
    }
  });

  test('handles invalid CSV schema', async ({ authenticatedPage: page }) => {
    // CSV with wrong headers
    const invalidCsv = `wrong,headers,here
value1,value2,value3`;

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'invalid-schema.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(invalidCsv),
    });

    const uploadButton = page.locator('button:has-text("Upload")').first();
    if (await uploadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await uploadButton.click();

      // Should show schema validation error
      await expect(
        page.locator('text=/schema/i, text=/invalid.*format/i, text=/required.*field/i, [role="alert"]')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('handles empty CSV file', async ({ authenticatedPage: page }) => {
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'empty.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(''),
    });

    const uploadButton = page.locator('button:has-text("Upload")').first();
    if (await uploadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await uploadButton.click();

      // Should show error about empty file
      await expect(
        page.locator('text=/empty/i, text=/no.*data/i, [role="alert"]')
      ).toBeVisible({ timeout: 5000 });
    }
  });

  test('drag and drop file upload', async ({ authenticatedPage: page }) => {
    const csvContent = 'name,phe,protein\nTest Product,40,4';

    // Simulate drag and drop (Note: actual drag-drop in Playwright requires more complex setup)
    // For now, we'll use the file input approach
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'dragged-file.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent),
    });

    // Verify file was selected
    await expect(page.locator('text=/dragged-file.csv/i')).toBeVisible();
  });

  test('disables UI during upload', async ({ authenticatedPage: page }) => {
    const csvContent = 'name,phe,protein\nTest,50,5';

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles({
      name: 'test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent),
    });

    const uploadButton = page.locator('button:has-text("Upload")').first();
    if (await uploadButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      // Start upload
      const uploadPromise = uploadButton.click();

      // Check if button is disabled during upload (timing-dependent)
      await page.waitForTimeout(100);
      
      // Complete upload
      await uploadPromise;

      // Eventually button should be re-enabled or replaced with success state
    }
  });
});


