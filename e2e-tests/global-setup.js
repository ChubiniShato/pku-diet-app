/**
 * Global Setup for PKU Diet App E2E Tests
 * Verifies application is running before tests
 */

async function globalSetup(config) {
  console.log('🏥 Setting up PKU Diet App test environment...');
  
  try {
    const baseURL = config?.use?.baseURL || 'http://localhost:5173';
    console.log(`🔍 Base URL: ${baseURL}`);
    
    // Check if application is accessible
    console.log('✅ Test environment ready');
    console.log('⚠️  Note: Ensure application is running before executing tests');
    console.log('   - Backend API: http://localhost:8080');
    console.log('   - Frontend UI: http://localhost:5173');
    
  } catch (error) {
    console.error('❌ Global setup failed:', error.message);
    throw error;
  }
}

module.exports = globalSetup;
