import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'
import type { CriticalFactFilters, CriticalFactType, CriticalFactSeverity, CriticalFactSource } from '@/lib/types'

interface CriticalFactsFiltersProps {
  filters: CriticalFactFilters
  onFiltersChange: (filters: CriticalFactFilters) => void
  onExport?: () => void
  isExporting?: boolean
}

export const CriticalFactsFilters: React.FC<CriticalFactsFiltersProps> = ({
  filters,
  onFiltersChange,
  onExport,
  isExporting = false,
}) => {
  const { t } = useTranslation()
  const [isExpanded, setIsExpanded] = useState(false)

  const handleTypeChange = (type: CriticalFactType, checked: boolean) => {
    const currentTypes = filters.type || []
    const newTypes = checked
      ? [...currentTypes, type]
      : currentTypes.filter(t => t !== type)
    
    onFiltersChange({ ...filters, type: newTypes, page: 0 })
  }

  const handleSeverityChange = (severity: CriticalFactSeverity, checked: boolean) => {
    const currentSeverities = filters.severity || []
    const newSeverities = checked
      ? [...currentSeverities, severity]
      : currentSeverities.filter(s => s !== severity)
    
    onFiltersChange({ ...filters, severity: newSeverities, page: 0 })
  }

  const handleSourceChange = (source: CriticalFactSource, checked: boolean) => {
    const currentSources = filters.source || []
    const newSources = checked
      ? [...currentSources, source]
      : currentSources.filter(s => s !== source)
    
    onFiltersChange({ ...filters, source: newSources, page: 0 })
  }

  const handleDateFromChange = (dateFrom: string) => {
    onFiltersChange({ ...filters, dateFrom, page: 0 })
  }

  const handleDateToChange = (dateTo: string) => {
    onFiltersChange({ ...filters, dateTo, page: 0 })
  }

  const handleResolvedChange = (resolved: boolean | undefined) => {
    onFiltersChange({ ...filters, resolved, page: 0 })
  }

  const clearFilters = () => {
    onFiltersChange({
      patientId: filters.patientId,
      page: 0,
      size: filters.size,
    })
  }

  const hasActiveFilters = () => {
    return !!(
      filters.type?.length ||
      filters.severity?.length ||
      filters.source?.length ||
      filters.dateFrom ||
      filters.dateTo ||
      filters.resolved !== undefined
    )
  }

  const getTypeLabel = (type: CriticalFactType): string => {
    switch (type) {
      case 'PHE_BREACH':
        return 'PHE Breach'
      case 'PROTEIN_DEFICIENCY':
        return 'Protein Deficiency'
      case 'CALORIE_EXCESS':
        return 'Calorie Excess'
      case 'CALORIE_DEFICIENCY':
        return 'Calorie Deficiency'
      case 'MICRONUTRIENT_DEFICIENCY':
        return 'Micronutrient Deficiency'
      case 'MEDICATION_INTERACTION':
        return 'Medication Interaction'
      case 'ALLERGEN_EXPOSURE':
        return 'Allergen Exposure'
      case 'FORBIDDEN_INGREDIENT':
        return 'Forbidden Ingredient'
      case 'SAFETY_WARNING':
        return 'Safety Warning'
      case 'DIETARY_VIOLATION':
        return 'Dietary Violation'
      default:
        return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
    }
  }

  const getSeverityLabel = (severity: CriticalFactSeverity): string => {
    return severity.charAt(0) + severity.slice(1).toLowerCase()
  }

  const getSourceLabel = (source: CriticalFactSource): string => {
    switch (source) {
      case 'DAILY_VALIDATION':
        return 'Daily Validation'
      case 'MEAL_ENTRY':
        return 'Meal Entry'
      case 'PRODUCT_SCAN':
        return 'Product Scan'
      case 'MANUAL_ENTRY':
        return 'Manual Entry'
      case 'AUTOMATED_CHECK':
        return 'Automated Check'
      case 'HEALTHCARE_PROVIDER':
        return 'Healthcare Provider'
      case 'SYSTEM_ALERT':
        return 'System Alert'
      default:
        return source.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Filter Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <h3 className="text-lg font-medium text-gray-900">
              Critical Facts Filters
            </h3>
            {hasActiveFilters() && (
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {[
                  (filters.type?.length || 0),
                  (filters.severity?.length || 0),
                  (filters.source?.length || 0),
                  filters.dateFrom ? 1 : 0,
                  filters.dateTo ? 1 : 0,
                  filters.resolved !== undefined ? 1 : 0,
                ].reduce((a, b) => a + b, 0)} active filters
              </span>
            )}
          </div>
          
          <div className="flex items-center space-x-3">
            {onExport && (
              <Button
                onClick={onExport}
                variant="secondary"
                size="sm"
                disabled={isExporting}
              >
                {isExporting ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-gray-600" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Exporting...
                  </>
                ) : (
                  <>
                    <svg className="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                    Export CSV
                  </>
                )}
              </Button>
            )}
            
            <button
              onClick={() => setIsExpanded(!isExpanded)}
              className="text-gray-500 hover:text-gray-700"
            >
              <svg className={`h-5 w-5 transform transition-transform ${isExpanded ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Quick Filters */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">Status:</label>
            <select
              value={filters.resolved === undefined ? 'all' : filters.resolved ? 'resolved' : 'unresolved'}
              onChange={(e) => {
                const value = e.target.value
                handleResolvedChange(
                  value === 'all' ? undefined : value === 'resolved'
                )
              }}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">All Facts</option>
              <option value="unresolved">Unresolved Only</option>
              <option value="resolved">Resolved Only</option>
            </select>
          </div>

          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">From:</label>
            <input
              type="date"
              value={filters.dateFrom || ''}
              onChange={(e) => handleDateFromChange(e.target.value)}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">To:</label>
            <input
              type="date"
              value={filters.dateTo || ''}
              onChange={(e) => handleDateToChange(e.target.value)}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {hasActiveFilters() && (
            <Button
              onClick={clearFilters}
              variant="secondary"
              size="sm"
            >
              Clear All
            </Button>
          )}
        </div>
      </div>

      {/* Expanded Filters */}
      {isExpanded && (
        <div className="px-6 py-4 space-y-6">
          {/* Type Filters */}
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-3">Fact Types</h4>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
              {Object.values(CriticalFactType).map((type) => (
                <label key={type} className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={filters.type?.includes(type) || false}
                    onChange={(e) => handleTypeChange(type, e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    {getTypeLabel(type)}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Severity Filters */}
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-3">Severity Levels</h4>
            <div className="flex flex-wrap gap-2">
              {Object.values(CriticalFactSeverity).map((severity) => (
                <label key={severity} className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={filters.severity?.includes(severity) || false}
                    onChange={(e) => handleSeverityChange(severity, e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className={`ml-2 text-sm font-medium px-2 py-1 rounded-full ${
                    severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                    severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                    severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-blue-100 text-blue-800'
                  }`}>
                    {getSeverityLabel(severity)}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Source Filters */}
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-3">Sources</h4>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
              {Object.values(CriticalFactSource).map((source) => (
                <label key={source} className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={filters.source?.includes(source) || false}
                    onChange={(e) => handleSourceChange(source, e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    {getSourceLabel(source)}
                  </span>
                </label>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
