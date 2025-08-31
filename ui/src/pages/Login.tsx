import React, { useEffect } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/contexts/AuthContext'
import { LoginForm } from '@/components/LoginForm'

export const Login: React.FC = () => {
  const { t } = useTranslation()
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  // Redirect authenticated users
  if (!isLoading && isAuthenticated) {
    const from = location.state?.from?.pathname || '/'
    return <Navigate to={from} replace />
  }

  // Show loading while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
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

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* App Logo/Branding */}
        <div className="text-center">
          <h1 className="text-4xl font-bold text-blue-600 mb-2">
            PKU Diet
          </h1>
          <p className="text-gray-600">
            {t('app.description')}
          </p>
        </div>

        {/* Login Form */}
        <LoginForm />

        {/* Additional Information */}
        <div className="text-center space-y-4">
          <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
            <h3 className="text-sm font-medium text-blue-800 mb-2">
              {t('auth.demo.title')}
            </h3>
            <div className="text-xs text-blue-700 space-y-1">
              <p><strong>Admin:</strong> admin / admin123</p>
              <p><strong>User:</strong> user / user123</p>
            </div>
          </div>
          
          <div className="text-xs text-gray-500">
            <p>{t('auth.security.notice')}</p>
          </div>
        </div>
      </div>
    </div>
  )
}
