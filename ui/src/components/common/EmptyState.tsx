import React from 'react'

interface EmptyStateProps {
  icon?: React.ComponentType<{ className?: string }>
  title: string
  description?: string
  action?: {
    label: string
    onClick: () => void
    variant?: 'primary' | 'secondary'
  }
  className?: string
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon: Icon,
  title,
  description,
  action,
  className = '',
}) => {
  return (
    <div
      className={`text-center py-12 px-4 ${className}`}
      role="status"
      aria-label={title}
    >
      {Icon && (
        <div className="mx-auto h-12 w-12 text-gray-400 mb-4">
          <Icon className="h-full w-full" aria-hidden="true" />
        </div>
      )}

      <h3 className="text-lg font-medium text-gray-900 mb-2">
        {title}
      </h3>

      {description && (
        <p className="text-gray-500 mb-6 max-w-sm mx-auto">
          {description}
        </p>
      )}

      {action && (
        <button
          onClick={action.onClick}
          className={`
            inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md
            ${action.variant === 'secondary'
              ? 'text-gray-700 bg-gray-100 hover:bg-gray-200 focus:ring-gray-500'
              : 'text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'
            }
            focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors duration-200
          `}
          aria-label={action.label}
        >
          {action.label}
        </button>
      )}
    </div>
  )
}

interface CriticalFactsEmptyStateProps {
  onCreateFact?: () => void
  className?: string
}

export const CriticalFactsEmptyState: React.FC<CriticalFactsEmptyStateProps> = ({
  onCreateFact,
  className,
}) => {
  const { t } = useTranslation()

  return (
    <EmptyState
      icon={({ className: iconClass }) => (
        <svg className={iconClass} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      )}
      title={t('critical.table.noFacts')}
      description={t('critical.table.noFactsDesc')}
      action={
        onCreateFact
          ? {
              label: t('common.add'),
              onClick: onCreateFact,
            }
          : undefined
      }
      className={className}
    />
  )
}

interface ProductsEmptyStateProps {
  onBrowseProducts?: () => void
  className?: string
}

export const ProductsEmptyState: React.FC<ProductsEmptyStateProps> = ({
  onBrowseProducts,
  className,
}) => {
  const { t } = useTranslation()

  return (
    <EmptyState
      icon={({ className: iconClass }) => (
        <svg className={iconClass} fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M9 9l8-4"
          />
        </svg>
      )}
      title={t('pages.products.title')}
      description="Start building your product database by browsing available items"
      action={
        onBrowseProducts
          ? {
              label: "Browse Products",
              onClick: onBrowseProducts,
            }
          : undefined
      }
      className={className}
    />
  )
}
