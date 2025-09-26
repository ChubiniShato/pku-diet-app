import React, { useEffect } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { QueryProvider } from '@/providers/QueryProvider'
import { AccessibilityProvider } from '@/components/accessibility/AccessibilityProvider'
import { AuthProvider, useAuth } from '@/contexts/AuthContext'
import { HelpProvider } from '@/contexts/HelpContext'
import { AppHeader } from '@/components/AppHeader'
import { AppRoutes } from '@/components/AppRoutes'
import { ErrorBoundary } from '@/components/ErrorBoundary'
import { ToastContainer } from '@/components/ToastContainer'
import { HelpModal } from '@/components/HelpModal'
import '@/i18n/config'

// Inner app component that handles auth events
const AppContent: React.FC = () => {
  const { logout } = useAuth()

  // Handle token expiration events from API client
  useEffect(() => {
    const handleTokenExpired = () => {
      logout()
    }

    window.addEventListener('auth:token-expired', handleTokenExpired)
    
    return () => {
      window.removeEventListener('auth:token-expired', handleTokenExpired)
    }
  }, [logout])

  return (
    <div className="min-h-screen bg-gray-50">
      <AppHeader />
      <main>
        <AppRoutes />
      </main>
      <ToastContainer />
      <HelpModal />
    </div>
  )
}

function App() {
  return (
    <ErrorBoundary>
      <QueryProvider>
        <AccessibilityProvider>
          <BrowserRouter>
            <AuthProvider>
              <HelpProvider>
                <AppContent />
              </HelpProvider>
            </AuthProvider>
          </BrowserRouter>
        </AccessibilityProvider>
      </QueryProvider>
    </ErrorBoundary>
  )
}

export default App
