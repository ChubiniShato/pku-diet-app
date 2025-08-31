import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/contexts/AuthContext'
import { hasRole, hasMinimumRole } from '@/lib/auth'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: string | string[]
  minimumRole?: string
  fallback?: React.ReactNode
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole,
  minimumRole,
  fallback
}) => {
  const { t } = useTranslation()
  const { isAuthenticated, isLoading, user } = useAuth()
  const location = useLocation()

  // Show loading while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="flex items-center space-x-2">
          <svg className="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span className="text-gray-600">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // Check role-based access
  if (user && (requiredRole || minimumRole)) {
    const userRole = user.role

    // Check specific role requirement
    if (requiredRole && !hasRole(userRole, requiredRole)) {
      return (
        <UnauthorizedAccess 
          fallback={fallback}
          requiredRole={Array.isArray(requiredRole) ? requiredRole : [requiredRole]}
          userRole={userRole}
        />
      )
    }

    // Check minimum role requirement
    if (minimumRole && !hasMinimumRole(userRole, minimumRole)) {
      return (
        <UnauthorizedAccess 
          fallback={fallback}
          minimumRole={minimumRole}
          userRole={userRole}
        />
      )
    }
  }

  // User is authenticated and authorized
  return <>{children}</>
}

// Unauthorized access component
interface UnauthorizedAccessProps {
  fallback?: React.ReactNode
  requiredRole?: string[]
  minimumRole?: string
  userRole: string
}

const UnauthorizedAccess: React.FC<UnauthorizedAccessProps> = ({
  fallback,
  requiredRole,
  minimumRole,
  userRole
}) => {
  const { t } = useTranslation()

  if (fallback) {
    return <>{fallback}</>
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white shadow-lg rounded-lg p-8 text-center">
        <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
          <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.464 0L4.35 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>

        <h3 className="text-lg font-medium text-gray-900 mb-2">
          {t('auth.unauthorized.title')}
        </h3>

        <p className="text-sm text-gray-600 mb-6">
          {t('auth.unauthorized.message')}
        </p>

        <div className="bg-gray-50 rounded-md p-4 mb-6">
          <div className="text-sm">
            <p className="text-gray-700 mb-2">
              <strong>{t('auth.unauthorized.yourRole')}:</strong> {t(`roles.${userRole.toLowerCase()}`)}
            </p>
            {requiredRole && (
              <p className="text-gray-700">
                <strong>{t('auth.unauthorized.requiredRole')}:</strong>{' '}
                {requiredRole.map(role => t(`roles.${role.toLowerCase()}`)).join(', ')}
              </p>
            )}
            {minimumRole && (
              <p className="text-gray-700">
                <strong>{t('auth.unauthorized.minimumRole')}:</strong>{' '}
                {t(`roles.${minimumRole.toLowerCase()}`)}
              </p>
            )}
          </div>
        </div>

        <div className="space-y-3">
          <button
            onClick={() => window.history.back()}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors"
          >
            {t('common.goBack')}
          </button>
          
          <button
            onClick={() => window.location.href = '/'}
            className="w-full bg-gray-600 text-white py-2 px-4 rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
          >
            {t('navigation.dashboard')}
          </button>
        </div>
      </div>
    </div>
  )
}

// Higher-order component for role-based access
export const withRoleProtection = <P extends object>(
  Component: React.ComponentType<P>,
  requiredRole?: string | string[],
  minimumRole?: string
) => {
  return React.forwardRef<any, P>((props, ref) => (
    <ProtectedRoute requiredRole={requiredRole} minimumRole={minimumRole}>
      <Component {...props} ref={ref} />
    </ProtectedRoute>
  ))
}

// Convenience components for common role checks
export const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requiredRole="ADMIN">
    {children}
  </ProtectedRoute>
)

export const HealthcareRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute minimumRole="HEALTHCARE_PROVIDER">
    {children}
  </ProtectedRoute>
)

export const PatientRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute minimumRole="PATIENT">
    {children}
  </ProtectedRoute>
)
