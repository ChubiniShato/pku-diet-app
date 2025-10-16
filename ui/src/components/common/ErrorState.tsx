import React from 'react'

interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
  retryLabel?: string
  className?: string
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title,
  message,
  onRetry,
  retryLabel,
  className = '',
}) => {
  return (
    <div
      className={`text-center py-12 px-4 ${className}`}
      role="alert"
      aria-live="assertive"
    >
      <div className="mx-auto h-12 w-12 text-red-400 mb-4">
        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
          />
        </svg>
      </div>

      <h3 className="text-lg font-medium text-gray-900 mb-2">
        {title || 'Error'}
      </h3>

      <p className="text-gray-500 mb-6 max-w-sm mx-auto">
        {message || 'Something went wrong. Please try again.'}
      </p>

      {onRetry && (
        <button
          onClick={onRetry}
          className="
            inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium
            rounded-md text-white bg-red-600 hover:bg-red-700
            focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500
            transition-colors duration-200
          "
          aria-label={retryLabel || 'Try again'}
        >
          <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
          {retryLabel || 'Try Again'}
        </button>
      )}
    </div>
  )
}

interface CriticalFactsErrorStateProps {
  onRetry?: () => void
  className?: string
}

export const CriticalFactsErrorState: React.FC<CriticalFactsErrorStateProps> = ({
  onRetry,
  className,
}) => {
  return (
    <ErrorState
      title="Unable to Load Critical Facts"
      message="We couldn't load your critical facts. Please check your connection and try again."
      onRetry={onRetry}
      retryLabel="Reload Critical Facts"
      className={className}
    />
  )
}

interface NetworkErrorStateProps {
  onRetry?: () => void
  className?: string
}

export const NetworkErrorState: React.FC<NetworkErrorStateProps> = ({
  onRetry,
  className,
}) => {
  return (
    <ErrorState
      title="Connection Error"
      message="Unable to connect to the server. Please check your internet connection and try again."
      onRetry={onRetry}
      retryLabel="Retry Connection"
      className={className}
    />
  )
}
