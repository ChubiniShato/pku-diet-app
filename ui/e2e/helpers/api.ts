import { APIRequestContext } from '@playwright/test';

/**
 * Helper to login and get auth token
 */
export async function login(
  request: APIRequestContext,
  username: string,
  password: string
): Promise<string> {
  const response = await request.post('/api/v1/auth/login', {
    data: { username, password },
  });
  
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()}`);
  }
  
  const body = await response.json();
  return body.token;
}

/**
 * Create a test patient
 */
export async function createPatient(
  request: APIRequestContext,
  token: string,
  patientData?: Partial<{
    name: string;
    pheLimit: number;
    proteinLimit: number;
    calorieLimit: number;
  }>
) {
  const defaultData = {
    name: 'Test Patient',
    pheLimit: 500,
    proteinLimit: 30,
    calorieLimit: 2000,
    ...patientData,
  };

  const response = await request.post('/api/v1/patients', {
    headers: { Authorization: `Bearer ${token}` },
    data: defaultData,
  });

  if (!response.ok()) {
    throw new Error(`Failed to create patient: ${response.status()}`);
  }

  return await response.json();
}

/**
 * Create a test product/dish
 */
export async function createProduct(
  request: APIRequestContext,
  token: string,
  productData?: Partial<{
    name: string;
    phe: number;
    protein: number;
    calories: number;
  }>
) {
  const defaultData = {
    name: 'Test Product',
    phe: 50,
    protein: 5,
    calories: 100,
    ...productData,
  };

  const response = await request.post('/api/v1/products', {
    headers: { Authorization: `Bearer ${token}` },
    data: defaultData,
  });

  if (!response.ok()) {
    throw new Error(`Failed to create product: ${response.status()}`);
  }

  return await response.json();
}


