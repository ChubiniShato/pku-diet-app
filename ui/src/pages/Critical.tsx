import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router-dom'
import { useCriticalFacts, useCriticalFactsSummary, useExportCriticalFacts } from '@/lib/api/criticalFacts'
import { CriticalFactsFilters } from '@/components/CriticalFactsFilters'
import { CriticalFactsTable } from '@/components/CriticalFactsTable'
import { toast } from '@/lib/toast/toast'
import type { CriticalFactFilters as CriticalFactFiltersType } from '@/lib/types'

export const Critical: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const patientId = 'current-patient' // TODO: Get actual patient ID
  
  const [filters, setFilters] = useState<CriticalFactFiltersType>({
    patientId,
    page: 0,
    size: 20,
    sort: 'date',
    sortDirection: 'DESC',
  })

  // Initialize filters from URL parameters
  useEffect(() => {
    const urlDate = searchParams.get('date')
    const urlDateFrom = searchParams.get('dateFrom')
    const urlDateTo = searchParams.get('dateTo')
    
    if (urlDate) {
      // Single date filter
      setFilters(prev => ({
        ...prev,
        dateFrom: urlDate,
        dateTo: urlDate,
      }))
    } else if (urlDateFrom || urlDateTo) {
      // Date range filter
      setFilters(prev => ({
        ...prev,
        dateFrom: urlDateFrom || undefined,
        dateTo: urlDateTo || undefined,
      }))
    }
  }, [searchParams])

  const { data: criticalFacts, isLoading } = useCriticalFacts(filters)
  const { data: summary } = useCriticalFactsSummary(patientId)
  const exportMutation = useExportCriticalFacts()

  const handleFiltersChange = (newFilters: CriticalFactFiltersType) => {
    setFilters(newFilters)
  }

  const handleExport = async () => {
    try {
      const exportResponse = await exportMutation.mutateAsync({
        filters,
        format: 'CSV',
        includeResolved: true,
      })
      
      // Create download link
      const link = document.createElement('a')
      link.href = exportResponse.downloadUrl
      link.download = exportResponse.filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      toast.success('Export Complete', `Downloaded ${exportResponse.recordCount} critical facts as ${exportResponse.filename}`)
    } catch (error) {
      // Error handled by global error handler
    }
  }

  const getSummaryStats = () => {
    if (!summary) return null

    return [
      {
        label: 'Total Facts',
        value: summary.totalFacts,
        color: 'text-gray-900',
        bgColor: 'bg-gray-100',
      },
      {
        label: 'Unresolved',
        value: summary.unresolvedFacts,
        color: 'text-red-900',
        bgColor: 'bg-red-100',
      },
      {
        label: 'Critical',
        value: summary.factsBySeverity.CRITICAL || 0,
        color: 'text-red-900',
        bgColor: 'bg-red-100',
      },
      {
        label: 'High Priority',
        value: summary.factsBySeverity.HIGH || 0,
        color: 'text-orange-900',
        bgColor: 'bg-orange-100',
      },
    ]
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.critical.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.critical.subtitle')}
        </p>
      </div>

      {/* Summary Stats */}
      {summary && (
        <div className="mb-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {getSummaryStats()?.map((stat, index) => (
            <div key={index} className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="flex items-center">
                <div className={`rounded-full p-3 ${stat.bgColor}`}>
                  <div className={`text-lg font-bold ${stat.color}`}>
                    {stat.value}
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="space-y-6">
        {/* Filters */}
        <CriticalFactsFilters
          filters={filters}
          onFiltersChange={handleFiltersChange}
          onExport={handleExport}
          isExporting={exportMutation.isLoading}
        />

        {/* Table */}
        {criticalFacts && (
          <CriticalFactsTable
            data={criticalFacts}
            filters={filters}
            onFiltersChange={handleFiltersChange}
            isLoading={isLoading}
          />
        )}
      </div>

      {/* Recent Facts Sidebar */}
      {summary?.recentFacts && summary.recentFacts.length > 0 && (
        <div className="mt-8">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              Recent Critical Facts
            </h3>
            <div className="space-y-3">
              {summary.recentFacts.slice(0, 5).map((fact) => (
                <div key={fact.id} className="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg">
                  <div className={`flex-shrink-0 w-2 h-2 rounded-full mt-2 ${
                    fact.severity === 'CRITICAL' ? 'bg-red-500' :
                    fact.severity === 'HIGH' ? 'bg-orange-500' :
                    fact.severity === 'MEDIUM' ? 'bg-yellow-500' :
                    'bg-blue-500'
                  }`}></div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {fact.type.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())}
                      </p>
                      <span className="text-xs text-gray-500">
                        {new Date(fact.date).toLocaleDateString()}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">
                      {fact.delta.nutrient}: {fact.delta.difference >= 0 ? '+' : ''}{fact.delta.difference.toFixed(1)} {fact.delta.unit}
                    </p>
                    {fact.context.productName && (
                      <p className="text-xs text-gray-500 mt-1">
                        {fact.context.productName}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
