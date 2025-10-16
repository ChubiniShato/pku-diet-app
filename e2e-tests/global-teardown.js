/**
 * Global Teardown for PKU Diet App E2E Tests
 * Cleans up after test execution
 */

async function globalTeardown(config) {
  console.log('🧹 Cleaning up PKU Diet App test environment...');
  
  try {
    console.log('✅ Global teardown completed');
    console.log('📊 View test results: npm run test:report');
    
  } catch (error) {
    console.error('❌ Global teardown failed:', error.message);
    // Don't throw error to avoid masking test failures
  }
}

module.exports = globalTeardown;
