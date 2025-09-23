import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { useProducts, useProductCategories } from '@/lib/api/products'
import { ProductCard } from '@/components/ProductCard'
import { ProductSearch } from '@/components/ProductSearch'
import { ProductDrawer } from '@/components/ProductDrawer'
import { CsvUpload } from '@/components/CsvUpload'
import { Pagination } from '@/components/Pagination'
import type { ProductSearchParams } from '@/lib/types'

export const Products: React.FC = () => {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [filters, setFilters] = useState<ProductSearchParams>({
    page: 0,
    size: 12,
    // Start with no category to hide products until selection
    category: undefined,
  })
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null)
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)

  // Check if admin mode is enabled via URL parameter
  const isAdminMode = new URLSearchParams(window.location.search).get('admin') === '1'

  const {
    data: productsResponse,
    isLoading,
    error,
    isFetching,
  } = useProducts(filters, i18n.language)

  // Load categories to render as buttons
  const { data: categories = [] } = useProductCategories(i18n.language)
  
  // Sort categories alphabetically based on current language
  const sortedCategories = [...categories].sort((a, b) => {
    // Use locale-specific sorting
    return a.localeCompare(b, i18n.language, { sensitivity: 'base' })
  })

  const handleFiltersChange = (newFilters: ProductSearchParams) => {
    setFilters(newFilters)
  }

  const handleCategoryClick = (category: string) => {
    // Navigate to category page instead of filtering on main page
    const encodedCategory = encodeURIComponent(category)
    navigate(`/products/category/${encodedCategory}`)
  }

  const handlePageChange = (page: number) => {
    setFilters(prev => ({ ...prev, page }))
  }

  const handleProductClick = (productId: string) => {
    setSelectedProductId(productId)
    setIsDrawerOpen(true)
  }

  const handleCloseDrawer = () => {
    setIsDrawerOpen(false)
    setSelectedProductId(null)
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          {t('pages.products.title')}
        </h1>
        <p className="mt-2 text-gray-600">
          {t('pages.products.subtitle')}
        </p>
      </div>

      {/* CSV Upload (Admin Only) */}
      <CsvUpload isVisible={isAdminMode} />

      {/* Search and Filters */}
      <ProductSearch
        filters={filters}
        onFiltersChange={handleFiltersChange}
      />

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">{t('common.loading')}</span>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">
                {t('common.error')}
              </h3>
              <div className="mt-2 text-sm text-red-700">
                <p>
                  Failed to load products. Please make sure your backend is running on port 8080.
                </p>
                {error instanceof Error && (
                  <p className="mt-1 font-mono text-xs">{error.message}</p>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Products Grid */}
      {productsResponse && (
        <>
          {/* Results Summary */}
          <div className="mb-6">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-700">
                Showing {productsResponse.numberOfElements} of {productsResponse.totalElements} products
                {isFetching && (
                  <span className="ml-2 text-blue-600">
                    <svg className="inline h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  </span>
                )}
              </p>
            </div>
          </div>

          {/* Category Buttons */}
          {sortedCategories.length > 0 && (
            <div className="mb-6">
              <div className="grid grid-cols-5 gap-2 mb-2">
                {/* All Products button in first position */}
                <button
                  onClick={() => navigate('/products/all')}
                  className={`w-full px-4 py-3 min-h-[4rem] rounded-lg border text-sm font-medium ${
                    !filters.category ? 'bg-gray-600 text-white border-gray-600' : 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100'
                  }`}
                >
                  {t('common.allProducts')}
                </button>
                {/* First 4 categories */}
                {sortedCategories.slice(0, 4).map((cat, index) => {
                  const colors = [
                    'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100',
                    'bg-green-50 text-green-700 border-green-200 hover:bg-green-100',
                    'bg-purple-50 text-purple-700 border-purple-200 hover:bg-purple-100',
                    'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100'
                  ]
                  return (
                    <button
                      key={cat}
                      onClick={() => handleCategoryClick(cat)}
                      className={`w-full px-4 py-3 min-h-[4rem] rounded-lg border text-sm font-medium ${colors[index]}`}
                    >
                      {cat}
                    </button>
                  )
                })}
              </div>
              <div className="grid grid-cols-5 gap-2 mb-2">
                {sortedCategories.slice(4, 9).map((cat, index) => {
                  const colors = [
                    'bg-pink-50 text-pink-700 border-pink-200 hover:bg-pink-100',
                    'bg-indigo-50 text-indigo-700 border-indigo-200 hover:bg-indigo-100',
                    'bg-teal-50 text-teal-700 border-teal-200 hover:bg-teal-100',
                    'bg-cyan-50 text-cyan-700 border-cyan-200 hover:bg-cyan-100',
                    'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'
                  ]
                  return (
                    <button
                      key={cat}
                      onClick={() => handleCategoryClick(cat)}
                      className={`w-full px-4 py-3 min-h-[4rem] rounded-lg border text-sm font-medium ${colors[index]}`}
                    >
                      {cat}
                    </button>
                  )
                })}
              </div>
              <div className="grid grid-cols-5 gap-2 mb-2">
                {sortedCategories.slice(9, 14).map((cat, index) => {
                  const colors = [
                    'bg-lime-50 text-lime-700 border-lime-200 hover:bg-lime-100',
                    'bg-amber-50 text-amber-700 border-amber-200 hover:bg-amber-100',
                    'bg-red-50 text-red-700 border-red-200 hover:bg-red-100',
                    'bg-rose-50 text-rose-700 border-rose-200 hover:bg-rose-100',
                    'bg-violet-50 text-violet-700 border-violet-200 hover:bg-violet-100'
                  ]
                  return (
                    <button
                      key={cat}
                      onClick={() => handleCategoryClick(cat)}
                      className={`w-full px-4 py-3 min-h-[4rem] rounded-lg border text-sm font-medium ${colors[index]}`}
                    >
                      {cat}
                    </button>
                  )
                })}
              </div>
            </div>
          )}

          {/* Products Grid - Hidden on main page */}
          {/* Products will be shown on individual category pages */}
        </>
      )}

      {/* Product Drawer */}
      <ProductDrawer
        productId={selectedProductId}
        isOpen={isDrawerOpen}
        onClose={handleCloseDrawer}
      />
    </div>
  )
}
