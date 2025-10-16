import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginForm } from '../LoginForm'
import { render, mockUnauthenticatedUser } from '@/test/test-utils'
import { useAuth } from '@/contexts/AuthContext'

// Mock the useAuth hook
vi.mock('@/contexts/AuthContext', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useAuth: vi.fn(),
  }
})

const mockUseAuth = vi.mocked(useAuth)

describe('LoginForm', () => {
  const mockLogin = vi.fn()
  const mockClearError = vi.fn()

  beforeEach(() => {
    mockLogin.mockReset()
    mockClearError.mockReset()
    
    mockUseAuth.mockReturnValue({
      ...mockUnauthenticatedUser,
      login: mockLogin,
      clearError: mockClearError,
    })
  })

  it('renders login form correctly', () => {
    render(<LoginForm />)

    expect(screen.getByRole('heading', { name: /sign in/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('shows validation errors for empty fields', async () => {
    const user = userEvent.setup()
    render(<LoginForm />)

    const submitButton = screen.getByRole('button', { name: /sign in/i })
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/username is required/i)).toBeInTheDocument()
      expect(screen.getByText(/password is required/i)).toBeInTheDocument()
    })
  })

  it('shows validation errors for short inputs', async () => {
    const user = userEvent.setup()
    render(<LoginForm />)

    const usernameInput = screen.getByLabelText(/username/i)
    const passwordInput = screen.getByLabelText(/password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(usernameInput, 'ab')
    await user.type(passwordInput, '123')
    await user.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/username must be at least 3 characters/i)).toBeInTheDocument()
      expect(screen.getByText(/password must be at least 6 characters/i)).toBeInTheDocument()
    })
  })

  it('submits form with valid data', async () => {
    const user = userEvent.setup()
    const mockOnSuccess = vi.fn()
    
    render(<LoginForm onSuccess={mockOnSuccess} />)

    const usernameInput = screen.getByLabelText(/username/i)
    const passwordInput = screen.getByLabelText(/password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })

    await user.type(usernameInput, 'testuser')
    await user.type(passwordInput, 'password123')
    await user.click(submitButton)

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      })
    })
  })

  it('displays auth error from context', () => {
    mockUseAuth.mockReturnValue({
      ...mockUnauthenticatedUser,
      error: 'Invalid credentials',
      login: mockLogin,
      clearError: mockClearError,
    })

    render(<LoginForm />)

    expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
  })

  it('shows loading state during login', () => {
    mockUseAuth.mockReturnValue({
      ...mockUnauthenticatedUser,
      isLoading: true,
      login: mockLogin,
      clearError: mockClearError,
    })

    render(<LoginForm />)

    expect(screen.getByText(/signing in/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled()
  })

  it('clears errors when user starts typing', async () => {
    const user = userEvent.setup()
    
    mockUseAuth.mockReturnValue({
      ...mockUnauthenticatedUser,
      error: 'Login failed',
      login: mockLogin,
      clearError: mockClearError,
    })

    render(<LoginForm />)

    const usernameInput = screen.getByLabelText(/username/i)
    await user.type(usernameInput, 'a')

    expect(mockClearError).toHaveBeenCalled()
  })

  it('toggles password visibility', async () => {
    const user = userEvent.setup()
    render(<LoginForm />)

    const passwordInput = screen.getByLabelText(/password/i)
    const toggleButton = screen.getByRole('button', { name: '' }) // Eye icon button

    expect(passwordInput).toHaveAttribute('type', 'password')

    await user.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'text')

    await user.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
  })
})
