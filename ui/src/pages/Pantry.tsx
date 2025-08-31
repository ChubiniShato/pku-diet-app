import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { PantryTable } from '@/components/PantryTable'
import { BudgetCard } from '@/components/BudgetCard'
import { Button } from '@/components/Button'
import type { PantryFilters } from '@/lib/types'

export const Pantry: React.FC = () => {
  const { t } = useTranslation()
  const [filters, setFilters] = useState<PantryFilters>({
    page: 0,
    size: 20,
  })
  const [searchQuery, setSearchQuery] = useState('')

  const handleSearch = () => {
    setFilters(prev => ({
      ...prev,
      query: searchQuery.trim() || undefined,
      page: 0,
    }))
  }

  const handleClearSearch = () => {
    setSearchQuery('')
    setFilters({ page: 0, size: 20 })
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch()
    }
  }

  const handleExpiringFilter = () => {
    setFilters(prev => ({
      ...prev,
      expiringWithinDays: prev.expiringWithinDays ? undefined : 7,
      page: 0,
    }))
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.pantry.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.pantry.subtitle')}
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="xl:col-span-2 space-y-6">
          {/* Search and Filters */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Search & Filter
            </h2>
            
            <div className="space-y-4">
              <div className="flex space-x-4">
                <div className="flex-1">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="Search pantry items..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <Button onClick={handleSearch} variant="primary">
                  {t('common.search')}
                </Button>
                <Button onClick={handleClearSearch} variant="secondary">
                  Clear
                </Button>
              </div>
              
              <div className="flex items-center space-x-4">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={filters.expiringWithinDays === 7}
                    onChange={handleExpiringFilter}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-700">Show expiring items (within 7 days)</span>
                </label>
              </div>

              {/* Active Filters */}
              {(filters.query || filters.expiringWithinDays) && (
                <div className="flex flex-wrap gap-2">
                  <span className="text-sm text-gray-600">Active filters:</span>
                  {filters.query && (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      Search: "{filters.query}"
                      <button
                        onClick={() => setFilters(prev => ({ ...prev, query: undefined }))}
                        className="ml-1 text-blue-600 hover:text-blue-800"
                      >
                        ×
                      </button>
                    </span>
                  )}
                  {filters.expiringWithinDays && (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                      Expiring within 7 days
                      <button
                        onClick={() => setFilters(prev => ({ ...prev, expiringWithinDays: undefined }))}
                        className="ml-1 text-yellow-600 hover:text-yellow-800"
                      >
                        ×
                      </button>
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Pantry Table */}
          <PantryTable
            filters={filters}
            onFiltersChange={setFilters}
          />
        </div>

        {/* Sidebar */}
        <div className="xl:col-span-1 space-y-6">
          {/* Budget Card */}
          <BudgetCard patientId="current-patient" />

          {/* Pantry Tips */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Pantry Tips
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Use expiring items first</p>
                  <p className="text-sm text-gray-600">
                    Items expiring within 7 days will be highlighted and prioritized in menu generation.
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Track prices</p>
                  <p className="text-sm text-gray-600">
                    Adding unit prices helps with budget planning and cost-effective meal generation.
                  </p>
                </div>
              </div>
              
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 8.172V5L8 4z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">Organize by location</p>
                  <p className="text-sm text-gray-600">
                    Note storage locations (Fridge, Freezer, Pantry) to better track your inventory.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Stats */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Quick Actions
            </h3>
            
            <div className="space-y-3">
              <button
                onClick={handleExpiringFilter}
                className="w-full flex items-center justify-between p-3 text-left border border-gray-200 rounded-lg hover:border-yellow-300 hover:bg-yellow-50 transition-colors"
              >
                <div className="flex items-center space-x-2">
                  <svg className="h-4 w-4 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <span className="text-sm font-medium text-gray-900">Check Expiring Items</span>
                </div>
                <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
              
              <button
                onClick={handleClearSearch}
                className="w-full flex items-center justify-between p-3 text-left border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-colors"
              >
                <div className="flex items-center space-x-2">
                  <svg className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                  </svg>
                  <span className="text-sm font-medium text-gray-900">View All Items</span>
                </div>
                <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
