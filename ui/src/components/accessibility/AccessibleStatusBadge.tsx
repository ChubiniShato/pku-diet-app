import React from 'react'
import { useTranslation } from 'react-i18next'
import type { CriticalFactSeverity } from '@/lib/types'

interface AccessibleStatusBadgeProps {
  severity: CriticalFactSeverity
  resolved?: boolean
  children: React.ReactNode
  className?: string
  onClick?: () => void
  tabIndex?: number
  role?: string
  'aria-label'?: string
}

export const AccessibleStatusBadge: React.FC<AccessibleStatusBadgeProps> = ({
  severity,
  resolved = false,
  children,
  className = '',
  onClick,
  tabIndex,
  role = 'status',
  'aria-label': ariaLabel,
}) => {
  const { t } = useTranslation()

  // WCAG AA compliant color combinations
  const getSeverityStyles = (severity: CriticalFactSeverity, resolved: boolean) => {
    if (resolved) {
      return {
        base: 'bg-green-100 text-green-800 border-green-200',
        focus: 'focus:ring-green-500 focus:border-green-500',
      }
    }

    switch (severity) {
      case 'CRITICAL':
        return {
          base: 'bg-red-100 text-red-800 border-red-300',
          focus: 'focus:ring-red-500 focus:border-red-500',
        }
      case 'HIGH':
        return {
          base: 'bg-orange-100 text-orange-800 border-orange-300',
          focus: 'focus:ring-orange-500 focus:border-orange-500',
        }
      case 'MEDIUM':
        return {
          base: 'bg-yellow-100 text-yellow-800 border-yellow-300',
          focus: 'focus:ring-yellow-500 focus:border-yellow-500',
        }
      case 'LOW':
        return {
          base: 'bg-blue-100 text-blue-800 border-blue-300',
          focus: 'focus:ring-blue-500 focus:border-blue-500',
        }
      default:
        return {
          base: 'bg-gray-100 text-gray-800 border-gray-300',
          focus: 'focus:ring-gray-500 focus:border-gray-500',
        }
    }
  }

  const styles = getSeverityStyles(severity, resolved)

  const defaultAriaLabel = resolved
    ? t('critical.table.resolved')
    : t(`critical.severity.${severity.toLowerCase()}`)

  const Component = onClick ? 'button' : 'span'

  return (
    <Component
      className={`
        inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
        border transition-colors duration-200
        ${styles.base}
        ${onClick ? `
          cursor-pointer hover:shadow-sm
          focus:outline-none focus:ring-2 focus:ring-offset-2 ${styles.focus}
        ` : ''}
        ${className}
      `}
      onClick={onClick}
      tabIndex={onClick ? (tabIndex ?? 0) : undefined}
      role={role}
      aria-label={ariaLabel || defaultAriaLabel}
      aria-pressed={onClick ? resolved : undefined}
    >
      {children}
    </Component>
  )
}
