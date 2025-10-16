import React from 'react'
import { QueryClient } from '@tanstack/react-query'
import { toast } from '@/lib/toast/toast'
import { ApiClientError } from '@/lib/api/client'

// Global error handler for TanStack Query
export function setupGlobalErrorHandler(queryClient: QueryClient) {
  queryClient.getQueryCache().config.onError = (error) => {
    handleApiError(error, 'Query failed')
  }

  queryClient.getMutationCache().config.onError = (error) => {
    handleApiError(error, 'Operation failed')
  }
}

function handleApiError(error: unknown, defaultTitle: string) {
  console.error('API Error:', error)

  if (error instanceof ApiClientError) {
    const title = getErrorTitle(error.status)
    const message = error.message

    // Don't show toast for 404s on optional queries
    if (error.status === 404) {
      console.warn('Resource not found:', message)
      return
    }

    toast.error(title, message, {
      duration: error.status >= 500 ? 10000 : 6000, // Server errors stay longer
    })
  } else if (error instanceof Error) {
    toast.error(defaultTitle, error.message)
  } else {
    toast.error(defaultTitle, 'An unexpected error occurred')
  }
}

function getErrorTitle(status: number): string {
  switch (true) {
    case status === 0:
      return 'Connection Error'
    case status === 400:
      return 'Invalid Request'
    case status === 401:
      return 'Authentication Required'
    case status === 403:
      return 'Access Denied'
    case status === 404:
      return 'Not Found'
    case status === 409:
      return 'Conflict'
    case status === 422:
      return 'Validation Error'
    case status >= 500:
      return 'Server Error'
    default:
      return 'Request Failed'
  }
}

interface ErrorProviderProps {
  children: React.ReactNode
  queryClient: QueryClient
}

export const ErrorProvider: React.FC<ErrorProviderProps> = ({ 
  children, 
  queryClient 
}) => {
  React.useEffect(() => {
    setupGlobalErrorHandler(queryClient)
  }, [queryClient])

  return <>{children}</>
}
