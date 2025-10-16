import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './Button'
import type { ProductSearchParams } from '@/lib/types'

interface ProductSearchProps {
  filters: ProductSearchParams
  onFiltersChange: (filters: ProductSearchParams) => void
}

export const ProductSearch: React.FC<ProductSearchProps> = ({
  filters,
  onFiltersChange,
}) => {
  const { t, i18n } = useTranslation()
  const [localQuery, setLocalQuery] = useState(filters.query || '')
  const [localMaxPhe, setLocalMaxPhe] = useState(filters.maxPhe?.toString() || '')

  const handleSearch = () => {
    onFiltersChange({
      ...filters,
      query: localQuery.trim() || undefined,
      maxPhe: localMaxPhe ? parseFloat(localMaxPhe) : undefined,
      page: 0, // Reset to first page when searching
    })
  }

  const handleLowPheToggle = (enabled: boolean) => {
    onFiltersChange({
      ...filters,
      maxPhe: enabled ? 100 : undefined, // 100mg is typical low-PHE threshold
      page: 0,
    })
    if (enabled) {
      setLocalMaxPhe('100')
    } else {
      setLocalMaxPhe('')
    }
  }

  const handleClearFilters = () => {
    setLocalQuery('')
    setLocalMaxPhe('')
    onFiltersChange({
      page: 0,
      size: filters.size,
    })
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch()
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
      <h2 className="text-lg font-medium text-gray-900 mb-4">
        {t('common.search')} & {t('common.filter')}
      </h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
        {/* Search Query */}
        <div>
          <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
            {t('common.productName')}
          </label>
          <input
            id="search"
            type="text"
            value={localQuery}
            onChange={(e) => setLocalQuery(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={t('common.searchProducts')}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        {/* Low PHE Toggle */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {t('common.lowPheFilter')}
          </label>
          <div className="flex items-center h-10">
            <label className="flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={filters.maxPhe === 100}
                onChange={(e) => handleLowPheToggle(e.target.checked)}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span className="ml-2 text-sm text-gray-700">≤100mg PHE</span>
            </label>
          </div>
        </div>

        {/* Max PHE Filter */}
        <div>
          <label htmlFor="maxPhe" className="block text-sm font-medium text-gray-700 mb-1">
            {t('common.customMaxPhe')}
          </label>
          <input
            id="maxPhe"
            type="number"
            min="0"
            step="0.1"
            value={localMaxPhe}
            onChange={(e) => setLocalMaxPhe(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={t('common.maxPheExample')}
            disabled={filters.maxPhe === 100}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
          />
        </div>

        {/* Actions */}
        <div className="flex items-end space-x-2">
          <Button
            onClick={handleSearch}
            variant="primary"
            className="flex-1"
          >
            {t('common.search')}
          </Button>
          <Button
            onClick={handleClearFilters}
            variant="secondary"
          >
            {t('common.clear')}
          </Button>
        </div>
      </div>

      {/* Active Filters Display */}
      {(filters.query || filters.category || filters.maxPhe) && (
        <div className="flex flex-wrap gap-2">
          <span className="text-sm text-gray-600">{t('common.activeFilters')}</span>
          {filters.query && (
            <span className="inline-flex items-center px-2 py-1 rounded-md bg-blue-100 text-blue-800 text-sm">
              {t('common.query')}: "{filters.query}"
            </span>
          )}
          {filters.category && (
            <span className="inline-flex items-center px-2 py-1 rounded-md bg-green-100 text-green-800 text-sm">
              {t('common.category')}: {filters.category}
            </span>
          )}
          {filters.maxPhe && (
            <span className="inline-flex items-center px-2 py-1 rounded-md bg-yellow-100 text-yellow-800 text-sm">
              {t('common.maxPhe')}: ≤{filters.maxPhe}mg
            </span>
          )}
        </div>
      )}
    </div>
  )
}
