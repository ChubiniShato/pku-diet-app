import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useResolveCriticalFact, useUnresolveCriticalFact } from '@/lib/api/criticalFacts'
import { Button } from './Button'
import { Pagination } from './Pagination'
import { AccessibleStatusBadge } from './accessibility/AccessibleStatusBadge'
import { TableSkeleton } from './common/SkeletonLoader'
import { CriticalFactsEmptyState } from './common/EmptyState'
import { CriticalFactsErrorState } from './common/ErrorState'
import { useAccessibility } from './accessibility/AccessibilityProvider'
import { toast } from '@/lib/toast/toast'
import type { CriticalFact, CriticalFactFilters, PageResponse } from '@/lib/types'

interface CriticalFactsTableProps {
  data: PageResponse<CriticalFact>
  filters: CriticalFactFilters
  onFiltersChange: (filters: CriticalFactFilters) => void
  isLoading?: boolean
  error?: Error | null
}

export const CriticalFactsTable: React.FC<CriticalFactsTableProps> = ({
  data,
  filters,
  onFiltersChange,
  isLoading = false,
  error = null,
}) => {
  const { t } = useTranslation()
  const { announceMessage } = useAccessibility()
  const [expandedRow, setExpandedRow] = useState<string | null>(null)
  const [resolvingIds, setResolvingIds] = useState<Set<string>>(new Set())

  const resolveMutation = useResolveCriticalFact()
  const unresolveMutation = useUnresolveCriticalFact()

  const handleSort = (field: string) => {
    const currentSort = filters.sort
    const currentDirection = filters.sortDirection || 'DESC'
    
    let newDirection: 'ASC' | 'DESC' = 'DESC'
    if (currentSort === field && currentDirection === 'DESC') {
      newDirection = 'ASC'
    }
    
    onFiltersChange({
      ...filters,
      sort: field,
      sortDirection: newDirection,
      page: 0,
    })
  }

  const handlePageChange = (page: number) => {
    onFiltersChange({ ...filters, page })
  }

  const handleResolve = async (fact: CriticalFact) => {
    setResolvingIds(prev => new Set([...prev, fact.id]))

    try {
      if (fact.resolved) {
        await unresolveMutation.mutateAsync(fact.id)
        toast.success('Fact Unresolved', 'Critical fact has been marked as unresolved.')
        announceMessage(`Critical fact has been marked as unresolved: ${fact.type}`, 'assertive')
      } else {
        await resolveMutation.mutateAsync({ id: fact.id })
        toast.success('Fact Resolved', 'Critical fact has been marked as resolved.')
        announceMessage(`Critical fact has been marked as resolved: ${fact.type}`, 'assertive')
      }
    } catch (error) {
      announceMessage('Failed to update critical fact status', 'assertive')
    } finally {
      setResolvingIds(prev => {
        const newSet = new Set(prev)
        newSet.delete(fact.id)
        return newSet
      })
    }
  }

  const getSeverityFromString = (severity: string): CriticalFact['severity'] => {
    switch (severity) {
      case 'CRITICAL':
        return 'CRITICAL'
      case 'HIGH':
        return 'HIGH'
      case 'MEDIUM':
        return 'MEDIUM'
      case 'LOW':
        return 'LOW'
      default:
        return 'LOW'
    }
  }

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString()
  }

  const formatDateTime = (dateString: string): string => {
    return new Date(dateString).toLocaleString()
  }

  const formatDelta = (fact: CriticalFact): string => {
    const { delta } = fact
    const sign = delta.difference >= 0 ? '+' : ''
    return `${sign}${delta.difference.toFixed(1)} ${delta.unit} (${sign}${delta.percentageChange.toFixed(1)}%)`
  }

  const formatContext = (fact: CriticalFact): string => {
    const { context } = fact
    const parts = []
    
    if (context.mealType) {
      parts.push(context.mealType)
    }
    
    if (context.productName) {
      parts.push(context.productName)
    } else if (context.dishName) {
      parts.push(context.dishName)
    }
    
    if (context.quantity && context.unit) {
      parts.push(`${context.quantity} ${context.unit}`)
    }
    
    return parts.join(' • ') || context.source.replace('_', ' ').toLowerCase()
  }

  const getSortIcon = (field: string) => {
    if (filters.sort !== field) {
      return (
        <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      )
    }
    
    return filters.sortDirection === 'ASC' ? (
      <svg className="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
      </svg>
    ) : (
      <svg className="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
      </svg>
    )
  }

  // Handle loading state
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">
            {t('critical.table.title')}
          </h3>
        </div>
        <div className="p-6">
          <TableSkeleton rows={5} columns={7} />
        </div>
      </div>
    )
  }

  // Handle error state
  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">
            {t('critical.table.title')}
          </h3>
        </div>
        <div className="p-6">
          <CriticalFactsErrorState onRetry={() => window.location.reload()} />
        </div>
      </div>
    )
  }

  return (
    <div
      className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
      role="region"
      aria-label={t('critical.table.title')}
    >
      {/* Table Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-medium text-gray-900" id="critical-facts-table-heading">
            {t('critical.table.title')} ({data.totalElements})
          </h2>
          {data.content.length > 0 && (
            <p className="text-sm text-gray-500" aria-live="polite">
              {t('critical.table.showing')} {data.content.length} {t('critical.table.of')} {data.totalElements} {t('critical.table.facts')}
            </p>
          )}
        </div>
      </div>

      {/* Table */}
      {data.content.length === 0 ? (
        <div className="p-6">
          <CriticalFactsEmptyState />
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table
              className="min-w-full divide-y divide-gray-200"
              role="table"
              aria-labelledby="critical-facts-table-heading"
              aria-describedby="critical-facts-table-description"
            >
              <caption id="critical-facts-table-description" className="sr-only">
                Critical facts table with sortable columns and actionable items
              </caption>
              <thead className="bg-gray-50">
                <tr role="row">
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-inset"
                    onClick={() => handleSort('date')}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        handleSort('date')
                      }
                    }}
                    tabIndex={0}
                    role="columnheader"
                    aria-sort={filters.sort === 'date' ? (filters.sortDirection === 'ASC' ? 'ascending' : 'descending') : 'none'}
                  >
                    <div className="flex items-center space-x-1">
                      <span>{t('critical.table.date')}</span>
                      {getSortIcon('date')}
                    </div>
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-inset"
                    onClick={() => handleSort('type')}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        handleSort('type')
                      }
                    }}
                    tabIndex={0}
                    role="columnheader"
                    aria-sort={filters.sort === 'type' ? (filters.sortDirection === 'ASC' ? 'ascending' : 'descending') : 'none'}
                  >
                    <div className="flex items-center space-x-1">
                      <span>{t('critical.table.type')}</span>
                      {getSortIcon('type')}
                    </div>
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-inset"
                    onClick={() => handleSort('severity')}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        handleSort('severity')
                      }
                    }}
                    tabIndex={0}
                    role="columnheader"
                    aria-sort={filters.sort === 'severity' ? (filters.sortDirection === 'ASC' ? 'ascending' : 'descending') : 'none'}
                  >
                    <div className="flex items-center space-x-1">
                      <span>{t('critical.table.severity')}</span>
                      {getSortIcon('severity')}
                    </div>
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    role="columnheader"
                  >
                    {t('critical.table.delta')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    role="columnheader"
                  >
                    {t('critical.table.context')}
                  </th>
                  <th
                    className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-inset"
                    onClick={() => handleSort('resolved')}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        handleSort('resolved')
                      }
                    }}
                    tabIndex={0}
                    role="columnheader"
                    aria-sort={filters.sort === 'resolved' ? (filters.sortDirection === 'ASC' ? 'ascending' : 'descending') : 'none'}
                  >
                    <div className="flex items-center space-x-1">
                      <span>{t('critical.table.status')}</span>
                      {getSortIcon('resolved')}
                    </div>
                  </th>
                  <th
                    className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
                    role="columnheader"
                  >
                    {t('critical.table.actions')}
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {data.content.map((fact, index) => (
                  <React.Fragment key={fact.id}>
                    <tr
                      className={`hover:bg-gray-50 ${fact.resolved ? 'opacity-60' : ''}`}
                      role="row"
                      aria-rowindex={index + 1}
                    >
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900" role="cell">
                        <time dateTime={fact.date}>
                          {formatDate(fact.date)}
                        </time>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap" role="cell">
                        <span
                          className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                          role="status"
                          aria-label={`Fact type: ${t(`critical.types.${fact.type.toLowerCase()}`)}`}
                        >
                          {t(`critical.types.${fact.type.toLowerCase()}`)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap" role="cell">
                        <AccessibleStatusBadge
                          severity={getSeverityFromString(fact.severity)}
                          resolved={false}
                        >
                          {t(`critical.severity.${fact.severity.toLowerCase()}`)}
                        </AccessibleStatusBadge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900" role="cell">
                        <div>
                          <div className="font-medium">{fact.delta.nutrient}</div>
                          <div
                            className={`text-xs ${fact.delta.difference >= 0 ? 'text-red-600' : 'text-green-600'}`}
                            aria-label={`Delta: ${formatDelta(fact)}`}
                          >
                            {formatDelta(fact)}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900 max-w-xs truncate" role="cell">
                        <span title={formatContext(fact)}>
                          {formatContext(fact)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap" role="cell">
                        <AccessibleStatusBadge
                          severity="LOW"
                          resolved={fact.resolved}
                          onClick={() => handleResolve(fact)}
                          aria-label={fact.resolved ? t('critical.table.resolved') : t('critical.table.unresolved')}
                        >
                          {fact.resolved ? t('critical.table.resolved') : t('critical.table.unresolved')}
                        </AccessibleStatusBadge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2" role="cell">
                        <button
                          onClick={() => setExpandedRow(expandedRow === fact.id ? null : fact.id)}
                          className="text-blue-600 hover:text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded px-2 py-1"
                          aria-expanded={expandedRow === fact.id}
                          aria-controls={`details-${fact.id}`}
                          aria-label={expandedRow === fact.id ? t('critical.table.hide') : t('critical.table.details')}
                        >
                          {expandedRow === fact.id ? t('critical.table.hide') : t('critical.table.details')}
                        </button>
                        <Button
                          onClick={() => handleResolve(fact)}
                          variant={fact.resolved ? 'secondary' : 'primary'}
                          size="sm"
                          disabled={resolvingIds.has(fact.id)}
                          aria-label={resolvingIds.has(fact.id)
                            ? t('critical.table.processing')
                            : fact.resolved
                              ? t('critical.table.unresolve')
                              : t('critical.table.resolve')
                          }
                        >
                          {resolvingIds.has(fact.id)
                            ? t('critical.table.processing')
                            : fact.resolved
                              ? t('critical.table.unresolve')
                              : t('critical.table.resolve')
                          }
                        </Button>
                      </td>
                    </tr>
                    
                    {/* Expanded Row Details */}
                    {expandedRow === fact.id && (
                      <tr role="row" aria-rowindex={index + 2}>
                        <td colSpan={7} className="px-6 py-4 bg-gray-50" role="cell" id={`details-${fact.id}`}>
                          <div className="space-y-4" role="region" aria-labelledby={`details-heading-${fact.id}`}>
                            <h3 id={`details-heading-${fact.id}`} className="sr-only">
                              Details for critical fact on {formatDate(fact.date)}
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              {/* Delta Details */}
                              <div role="region" aria-labelledby={`delta-heading-${fact.id}`}>
                                <h4 id={`delta-heading-${fact.id}`} className="text-sm font-medium text-gray-900 mb-2">
                                  {t('critical.details.deltaDetails')}
                                </h4>
                                <div className="bg-white rounded-md p-3 space-y-2">
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">{t('critical.details.expected')}:</span>
                                    <span className="text-sm font-medium">{fact.delta.expectedValue} {fact.delta.unit}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">{t('critical.details.actual')}:</span>
                                    <span className="text-sm font-medium">{fact.delta.actualValue} {fact.delta.unit}</span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">{t('critical.details.difference')}:</span>
                                    <span className={`text-sm font-medium ${fact.delta.difference >= 0 ? 'text-red-600' : 'text-green-600'}`}>
                                      {formatDelta(fact)}
                                    </span>
                                  </div>
                                  {fact.delta.threshold && (
                                    <div className="flex justify-between">
                                      <span className="text-sm text-gray-600">{t('critical.details.threshold')}:</span>
                                      <span className="text-sm font-medium">{fact.delta.threshold} {fact.delta.unit} ({fact.delta.thresholdType})</span>
                                    </div>
                                  )}
                                </div>
                              </div>

                              {/* Context Details */}
                              <div role="region" aria-labelledby={`context-heading-${fact.id}`}>
                                <h4 id={`context-heading-${fact.id}`} className="text-sm font-medium text-gray-900 mb-2">
                                  {t('critical.details.contextDetails')}
                                </h4>
                                <div className="bg-white rounded-md p-3 space-y-2">
                                  <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">{t('critical.details.source')}:</span>
                                    <span className="text-sm font-medium">{t(`critical.sources.${fact.context.source.toLowerCase()}`)}</span>
                                  </div>
                                  {fact.context.mealType && (
                                    <div className="flex justify-between">
                                      <span className="text-sm text-gray-600">{t('critical.details.meal')}:</span>
                                      <span className="text-sm font-medium">{fact.context.mealType}</span>
                                    </div>
                                  )}
                                  {fact.context.productName && (
                                    <div className="flex justify-between">
                                      <span className="text-sm text-gray-600">{t('critical.details.product')}:</span>
                                      <span className="text-sm font-medium">{fact.context.productName}</span>
                                    </div>
                                  )}
                                  {fact.context.dishName && (
                                    <div className="flex justify-between">
                                      <span className="text-sm text-gray-600">{t('critical.details.dish')}:</span>
                                      <span className="text-sm font-medium">{fact.context.dishName}</span>
                                    </div>
                                  )}
                                  {fact.context.quantity && fact.context.unit && (
                                    <div className="flex justify-between">
                                      <span className="text-sm text-gray-600">{t('critical.details.quantity')}:</span>
                                      <span className="text-sm font-medium">{fact.context.quantity} {fact.context.unit}</span>
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>

                            {/* Timestamps */}
                            <div role="region" aria-labelledby={`timestamps-heading-${fact.id}`}>
                              <h4 id={`timestamps-heading-${fact.id}`} className="text-sm font-medium text-gray-900 mb-2">
                                {t('critical.details.timestamps')}
                              </h4>
                              <div className="bg-white rounded-md p-3 space-y-2 text-sm">
                                <div className="flex justify-between">
                                  <span className="text-gray-600">{t('critical.details.created')}:</span>
                                  <time className="font-medium" dateTime={fact.createdAt}>
                                    {formatDateTime(fact.createdAt)}
                                  </time>
                                </div>
                                {fact.resolved && fact.resolvedAt && (
                                  <div className="flex justify-between">
                                    <span className="text-gray-600">{t('critical.details.resolved')}:</span>
                                    <time className="font-medium" dateTime={fact.resolvedAt}>
                                      {formatDateTime(fact.resolvedAt)}
                                    </time>
                                  </div>
                                )}
                                {fact.notes && (
                                  <div>
                                    <span className="text-gray-600">{t('critical.details.notes')}:</span>
                                    <p className="mt-1 text-gray-900">{fact.notes}</p>
                                  </div>
                                )}
                              </div>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200">
              <Pagination
                currentPage={data.number}
                totalPages={data.totalPages}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </>
      )}
    </div>
  )
}
