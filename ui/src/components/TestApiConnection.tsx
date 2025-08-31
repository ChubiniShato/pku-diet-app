import React from 'react'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { apiClient } from '@/lib/api/client'
import { Button } from './Button'

export const TestApiConnection: React.FC = () => {
  const { t } = useTranslation()

  const {
    data,
    error,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['api-health'],
    queryFn: () => apiClient.checkHealth(),
    enabled: false, // Don't auto-fetch, only when button is clicked
    retry: false,
  })

  const handleTest = () => {
    refetch()
  }

  const getStatusColor = () => {
    if (isLoading) return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    if (error) return 'bg-red-100 text-red-800 border-red-200'
    if (data?.status === 'UP') return 'bg-green-100 text-green-800 border-green-200'
    return 'bg-gray-100 text-gray-800 border-gray-200'
  }

  const getStatusText = () => {
    if (isLoading) return t('api.testing')
    if (error) return t('api.status.down')
    if (data?.status === 'UP') return t('api.status.up')
    return ''
  }

  return (
    <div className="space-y-4">
      <Button
        onClick={handleTest}
        disabled={isLoading}
        variant="primary"
        size="lg"
      >
        {isLoading ? t('api.testing') : t('api.testConnection')}
      </Button>

      {(data || error) && (
        <div className={`p-4 rounded-md border ${getStatusColor()}`}>
          <div className="flex items-center">
            <div className="flex-shrink-0">
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-yellow-600 border-t-transparent rounded-full animate-spin" />
              ) : error ? (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              )}
            </div>
            <div className="ml-3">
              <p className="text-sm font-medium">
                {getStatusText()}
              </p>
              {error && (
                <p className="text-xs mt-1 opacity-75">
                  {error instanceof Error ? error.message : 'Connection failed'}
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
