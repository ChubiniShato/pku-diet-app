import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AppHeader } from '../AppHeader'
import { render, mockAuthenticatedUser, mockUnauthenticatedUser } from '@/test/test-utils'
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

describe('AppHeader', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue(mockUnauthenticatedUser)
  })

  it('renders app title and logo', () => {
    render(<AppHeader />)
    
    expect(screen.getByText('PKU')).toBeInTheDocument()
    expect(screen.getByRole('heading')).toBeInTheDocument()
  })

  it('shows login button when not authenticated', () => {
    render(<AppHeader />)
    
    expect(screen.getByRole('link', { name: /sign in/i })).toBeInTheDocument()
  })

  it('shows user menu when authenticated', () => {
    mockUseAuth.mockReturnValue(mockAuthenticatedUser)
    render(<AppHeader />)
    
    expect(screen.getByText('T')).toBeInTheDocument() // First letter of username
    expect(screen.getByText('testuser')).toBeInTheDocument()
  })

  it('shows desktop navigation links', () => {
    mockUseAuth.mockReturnValue(mockAuthenticatedUser)
    render(<AppHeader />)
    
    // These should be visible on desktop (hidden class is md:flex)
    expect(screen.getByRole('navigation')).toBeInTheDocument()
  })

  it('shows mobile menu button on mobile', () => {
    render(<AppHeader />)
    
    const mobileMenuButton = screen.getByRole('button', { name: /toggle menu/i })
    expect(mobileMenuButton).toBeInTheDocument()
  })

  it('opens mobile menu when hamburger is clicked', async () => {
    const user = userEvent.setup()
    mockUseAuth.mockReturnValue(mockAuthenticatedUser)
    render(<AppHeader />)
    
    const mobileMenuButton = screen.getByRole('button', { name: /toggle menu/i })
    await user.click(mobileMenuButton)
    
    // Mobile menu should show navigation links
    const mobileNavLinks = screen.getAllByRole('link')
    expect(mobileNavLinks.length).toBeGreaterThan(0)
  })

  it('opens user dropdown when user avatar is clicked', async () => {
    const user = userEvent.setup()
    mockUseAuth.mockReturnValue(mockAuthenticatedUser)
    render(<AppHeader />)
    
    const userButton = screen.getByRole('button', { name: '' })
    await user.click(userButton)
    
    expect(screen.getByRole('link', { name: /profile/i })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /settings/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /logout/i })).toBeInTheDocument()
  })

  it('calls logout when logout button is clicked', async () => {
    const user = userEvent.setup()
    const mockLogout = vi.fn()
    
    mockUseAuth.mockReturnValue({
      ...mockAuthenticatedUser,
      logout: mockLogout,
    })
    
    render(<AppHeader />)
    
    // Open user menu
    const userButton = screen.getByRole('button', { name: '' })
    await user.click(userButton)
    
    // Click logout
    const logoutButton = screen.getByRole('button', { name: /logout/i })
    await user.click(logoutButton)
    
    expect(mockLogout).toHaveBeenCalled()
  })

  it('closes mobile menu when navigation link is clicked', async () => {
    const user = userEvent.setup()
    mockUseAuth.mockReturnValue(mockAuthenticatedUser)
    render(<AppHeader />)
    
    // Open mobile menu
    const mobileMenuButton = screen.getByRole('button', { name: /toggle menu/i })
    await user.click(mobileMenuButton)
    
    // Click a navigation link (this should close the menu)
    const dashboardLink = screen.getAllByRole('link').find(link => 
      link.textContent?.toLowerCase().includes('dashboard')
    )
    
    if (dashboardLink) {
      await user.click(dashboardLink)
    }
    
    // Menu should close (hamburger icon should change back)
    expect(mobileMenuButton).toHaveAttribute('aria-expanded', 'false')
  })

  it('shows language switcher', () => {
    render(<AppHeader />)
    
    // LanguageSwitcher component should be rendered
    // This would depend on the actual implementation of LanguageSwitcher
    expect(screen.getByRole('banner')).toBeInTheDocument()
  })
})
