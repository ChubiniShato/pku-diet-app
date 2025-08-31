import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from '@/components/Button'
import { LoginRequest } from '@/lib/types'

interface LoginFormProps {
  onSuccess?: () => void
  className?: string
}

export const LoginForm: React.FC<LoginFormProps> = ({ 
  onSuccess, 
  className = '' 
}) => {
  const { t } = useTranslation()
  const { login, isLoading, error, clearError } = useAuth()
  
  const [formData, setFormData] = useState<LoginRequest>({
    username: '',
    password: ''
  })
  
  const [formErrors, setFormErrors] = useState<Partial<LoginRequest>>({})
  const [showPassword, setShowPassword] = useState(false)

  // Form validation
  const validateForm = (): boolean => {
    const errors: Partial<LoginRequest> = {}
    
    if (!formData.username.trim()) {
      errors.username = t('auth.validation.usernameRequired')
    } else if (formData.username.length < 3) {
      errors.username = t('auth.validation.usernameMinLength')
    }
    
    if (!formData.password.trim()) {
      errors.password = t('auth.validation.passwordRequired')
    } else if (formData.password.length < 6) {
      errors.password = t('auth.validation.passwordMinLength')
    }
    
    setFormErrors(errors)
    return Object.keys(errors).length === 0
  }

  // Handle input changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    
    // Clear field error when user starts typing
    if (formErrors[name as keyof LoginRequest]) {
      setFormErrors(prev => ({ ...prev, [name]: undefined }))
    }
    
    // Clear auth error when user modifies form
    if (error) {
      clearError()
    }
  }

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) return
    
    try {
      await login(formData)
      onSuccess?.()
    } catch (err) {
      // Error is handled by AuthContext
      console.error('Login failed:', err)
    }
  }

  return (
    <div className={`w-full max-w-md mx-auto ${className}`}>
      <div className="bg-white shadow-lg rounded-lg p-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-gray-900">
            {t('auth.login.title')}
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            {t('auth.login.subtitle')}
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-800">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Username Field */}
          <div>
            <label 
              htmlFor="username" 
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              {t('auth.login.username')}
            </label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              required
              value={formData.username}
              onChange={handleChange}
              className={`w-full px-3 py-2 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                formErrors.username ? 'border-red-300' : 'border-gray-300'
              }`}
              placeholder={t('auth.login.usernamePlaceholder')}
              disabled={isLoading}
            />
            {formErrors.username && (
              <p className="mt-1 text-sm text-red-600">{formErrors.username}</p>
            )}
          </div>

          {/* Password Field */}
          <div>
            <label 
              htmlFor="password" 
              className="block text-sm font-medium text-gray-700 mb-2"
            >
              {t('auth.login.password')}
            </label>
            <div className="relative">
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                required
                value={formData.password}
                onChange={handleChange}
                className={`w-full px-3 py-2 pr-10 border rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${
                  formErrors.password ? 'border-red-300' : 'border-gray-300'
                }`}
                placeholder={t('auth.login.passwordPlaceholder')}
                disabled={isLoading}
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 pr-3 flex items-center"
                onClick={() => setShowPassword(!showPassword)}
                disabled={isLoading}
              >
                <svg 
                  className="h-5 w-5 text-gray-400 hover:text-gray-600" 
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  {showPassword ? (
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                  ) : (
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  )}
                </svg>
              </button>
            </div>
            {formErrors.password && (
              <p className="mt-1 text-sm text-red-600">{formErrors.password}</p>
            )}
          </div>

          {/* Submit Button */}
          <div>
            <Button
              type="submit"
              className="w-full"
              disabled={isLoading}
            >
              {isLoading ? (
                <div className="flex items-center justify-center">
                  <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  {t('auth.login.signingIn')}
                </div>
              ) : (
                t('auth.login.signIn')
              )}
            </Button>
          </div>
        </form>

        {/* Footer */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            {t('auth.login.demoCredentials')}
          </p>
          <p className="text-xs text-gray-500 mt-1">
            {t('auth.login.demoNote')}
          </p>
        </div>
      </div>
    </div>
  )
}
