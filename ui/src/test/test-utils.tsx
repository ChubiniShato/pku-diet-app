import React, { ReactElement } from 'react'
import { render, RenderOptions } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
// Import AuthProvider conditionally to avoid mocking conflicts
// import { AuthProvider } from '@/contexts/AuthContext'
import { ErrorBoundary } from '@/components/ErrorBoundary'
import { AccessibilityProvider } from '@/components/accessibility/AccessibilityProvider'
import '@/i18n/config'

// Create a test query client with disabled retries and caching
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        cacheTime: 0,
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  })

interface AllTheProvidersProps {
  children: React.ReactNode
}

const AllTheProviders = ({ children }: AllTheProvidersProps) => {
  const queryClient = createTestQueryClient()

  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AccessibilityProvider>
          <BrowserRouter>
            {children}
          </BrowserRouter>
        </AccessibilityProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  )
}

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  wrapper?: React.ComponentType<any>
}

const customRender = (
  ui: ReactElement,
  options?: CustomRenderOptions
) => render(ui, { wrapper: AllTheProviders, ...options })

export * from '@testing-library/react'
export { customRender as render }

// Mock utilities
export const mockUser = {
  id: '1',
  username: 'testuser',
  email: 'test@example.com',
  role: 'USER' as const,
  createdAt: '2023-01-01T00:00:00Z',
  updatedAt: '2023-01-01T00:00:00Z',
  isEnabled: true,
  isAccountNonExpired: true,
  isAccountNonLocked: true,
  isCredentialsNonExpired: true,
}

export const mockAuthenticatedUser = {
  user: mockUser,
  token: 'mock-jwt-token',
  isAuthenticated: true,
  isLoading: false,
  error: null,
  login: vi.fn(),
  logout: vi.fn(),
  refreshToken: vi.fn(),
  clearError: vi.fn(),
}

export const mockUnauthenticatedUser = {
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  login: vi.fn(),
  logout: vi.fn(),
  refreshToken: vi.fn(),
  clearError: vi.fn(),
}
