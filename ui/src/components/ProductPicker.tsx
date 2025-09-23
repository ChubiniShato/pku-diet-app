import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useProducts, useProductCategories } from '@/lib/api/products'
import { Button } from './Button'
import type { Product, ProductSearchParams } from '@/lib/types'

interface ProductPickerProps {
  isOpen: boolean
  onClose: () => void
  onSelect: (product: Product) => void
  excludeIds?: string[]
}

export const ProductPicker: React.FC<ProductPickerProps> = ({
  isOpen,
  onClose,
  onSelect,
  excludeIds = [],
}) => {
  const { t, i18n } = useTranslation()
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('')
  const [filters, setFilters] = useState<ProductSearchParams>({
    page: 0,
    size: 20,
  })

  const { data: productsResponse, isLoading } = useProducts(filters)
  const { data: categories = [] } = useProductCategories(i18n.language)

  const handleSearch = () => {
    setFilters({
      ...filters,
      query: searchQuery.trim() || undefined,
      category: selectedCategory || undefined,
      page: 0,
    })
  }

  const handleClear = () => {
    setSearchQuery('')
    setSelectedCategory('')
    setFilters({
      page: 0,
      size: 20,
    })
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch()
    }
  }

  const handleProductSelect = (product: Product) => {
    onSelect(product)
    onClose()
  }

  const formatNutrient = (value?: number): string => {
    if (value === null || value === undefined) return '-'
    return value.toFixed(1)
  }

  const getPheColor = (phe?: number): string => {
    if (!phe) return 'text-gray-500'
    if (phe <= 50) return 'text-green-600'
    if (phe <= 100) return 'text-yellow-600'
    return 'text-red-600'
  }

  const filteredProducts = productsResponse?.content.filter(
    product => !excludeIds.includes(product.id)
  ) || []

  if (!isOpen) return null

  return (
    <>
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 z-50"
        onClick={onClose}
      />
      
      {/* Modal */}
      <div className="fixed inset-0 z-50 overflow-y-auto">
        <div className="flex items-center justify-center min-h-full p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold text-gray-900">
                  Select Product
                </h2>
                <button
                  onClick={onClose}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Search */}
            <div className="px-6 py-4 border-b border-gray-100">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-2">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="Search products..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <select
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">All Categories</option>
                    {categories.map((category) => (
                      <option key={category} value={category}>
                        {category}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="flex space-x-2 mt-4">
                <Button onClick={handleSearch} variant="primary" size="sm">
                  {t('common.search')}
                </Button>
                <Button onClick={handleClear} variant="secondary" size="sm">
                  Clear
                </Button>
              </div>
            </div>

            {/* Content */}
            <div className="px-6 py-4 max-h-96 overflow-y-auto">
              {isLoading && (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                  <span className="ml-3 text-gray-600">{t('common.loading')}</span>
                </div>
              )}

              {!isLoading && filteredProducts.length === 0 && (
                <div className="text-center py-8">
                  <p className="text-gray-500">No products found matching your criteria.</p>
                </div>
              )}

              {!isLoading && filteredProducts.length > 0 && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {filteredProducts.map((product) => (
                    <button
                      key={product.id}
                      onClick={() => handleProductSelect(product)}
                      className="text-left p-4 border border-gray-200 rounded-lg hover:border-blue-300 hover:shadow-sm transition-all"
                    >
                      <div className="flex justify-between items-start mb-2">
                        <div className="flex-1">
                          <h4 className="font-medium text-gray-900 text-sm">
                            {product.productName}
                          </h4>
                          <p className="text-xs text-gray-500 mt-1">
                            {product.category}
                          </p>
                        </div>
                        <div className="text-right">
                          <span className={`text-sm font-semibold ${getPheColor(product.phenylalanine)}`}>
                            {formatNutrient(product.phenylalanine)}
                          </span>
                          <p className="text-xs text-gray-500">mg PHE</p>
                        </div>
                      </div>
                      
                      <div className="grid grid-cols-3 gap-2 text-xs text-gray-600">
                        <div>
                          <span className="font-medium">{formatNutrient(product.protein)}g</span>
                          <span className="block text-gray-500">Protein</span>
                        </div>
                        <div>
                          <span className="font-medium">{formatNutrient(product.kilocalories)}</span>
                          <span className="block text-gray-500">kcal</span>
                        </div>
                        <div>
                          <span className="font-medium">{formatNutrient(product.carbohydrates)}g</span>
                          <span className="block text-gray-500">Carbs</span>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Footer */}
            <div className="px-6 py-4 border-t border-gray-200">
              <div className="flex justify-end">
                <Button onClick={onClose} variant="secondary">
                  Cancel
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
